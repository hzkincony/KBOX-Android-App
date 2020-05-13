package com.kincony.KControl.net.internal

import com.kincony.KControl.net.internal.connection.RealConnectionPool
import com.kincony.KControl.net.internal.converter.Converter
import com.kincony.KControl.net.internal.interfaces.Callback
import com.kincony.KControl.net.internal.interfaces.ResponseBody
import java.io.IOException
import java.io.InterruptedIOException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException


class Call(
    var client: Client?, @get:JvmName("request") var request: Request?,
    private var factory: Converter.Factory? = null
) {
    private var executed = false
    val host: String
        get() = request?.ip + ":" + request?.port

    fun enqueue(responseCallback: Callback) {
        synchronized(this) {
            check(!executed) { "Already Executed" }
            executed = true
        }
        client?.dispatcher?.enqueue(AsyncCall(responseCallback))
    }

    @Synchronized
    fun isExecuted(): Boolean = executed

    @Throws(IOException::class)
    internal fun getResponse(): ResponseBody? {
        var realConnection = RealConnectionPool.getRealConnection(this@Call)
        var result = realConnection.request(factory).syncReadConnect()
        return factory?.responseBodyConverter()?.convert(result)

    }


    internal inner class AsyncCall(
        private val responseCallback: Callback
    ) : Runnable {
        private val host: String
            get() = this@Call.host

        val request: Request?
            get() = this@Call.request

        val call: Call
            get() = this@Call

        fun executeOn(executorService: ExecutorService) {
            client?.dispatcher?.assertThreadDoesntHoldLock()
            Dispatcher.MAIN.post {
                responseCallback.onStart()
            }
            var success = false
            try {
                executorService.execute(this)
                success = true
            } catch (e: RejectedExecutionException) {
                val ioException = InterruptedIOException("executor rejected")
                ioException.initCause(e)
                responseCallback.onFailure(ioException)
            } finally {
                if (!success) {
                    client?.dispatcher?.finished(this) // This call is no longer running!
                }
            }
        }

        override fun run() {
            threadName("Call $host ${request?.request}") {
                var signalledCallback = false
                try {
                    val response = getResponse()
                    signalledCallback = true
                    Dispatcher.MAIN.post {
                        if (response?.isSucceed() == true) {
                            responseCallback.onResponse(response)
                        } else {
                            responseCallback.onFailure(Exception("ERROR"))
                        }
                    }
                } catch (e: IOException) {
                    if (signalledCallback) {
//                        Platform.get().log("Callback failure for ${toLoggableString()}", Platform.INFO, e)
                    } else {
                    }
                    Dispatcher.MAIN.post {
                        responseCallback.onFailure(e)
                    }
                } catch (t: Throwable) {
                    if (!signalledCallback) {
                        val canceledException = IOException("canceled due to $t")
                        canceledException.addSuppressed(t)
                        responseCallback.onFailure(canceledException)
                        Dispatcher.MAIN.post {
                            responseCallback.onFailure(canceledException)
                        }
                    }
                    throw t
                } finally {
                    client?.dispatcher?.finished(this)
                }
            }
        }
    }
}