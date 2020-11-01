package com.kincony.KControl.net.internal.connection

import android.os.Build
import com.kincony.KControl.net.internal.Call
import com.kincony.KControl.net.internal.Request
import com.kincony.KControl.net.internal.closeQuietly
import com.kincony.KControl.net.internal.converter.Converter
import com.kincony.KControl.net.internal.threadFactory
import com.kincony.KControl.utils.LogUtils
import okio.*
import java.io.IOException
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory

class RealConnection() {
    private var rawSocket: Socket? = null
    private var source: BufferedSource? = null
    private var sink: BufferedSink? = null
    private var codec: ExchangeCodec? = null
    private var call: Call? = null
    private var task: ScheduledFuture<*>? = null
    var lastHost: String? = null
    var inUse = false
        private set

    fun setNextCall(call: Call): RealConnection {
        this.call = call
        lastHost = call.host
        inUse = true
        return this
    }

    fun request(factory: Converter.Factory? = null): RealConnection {
        try {
            task?.cancel(false)
            if (rawSocket?.isConnected != true) {
                connectSocket(call)
            }
            sendRequest(call?.request, factory)
            task = executor.schedule({
                rawSocket?.closeQuietly()
                call?.request?.clearUse()
                call?.request?.recycle()
                call?.request = null
                rawSocket = null
                source = null
                sink = null
                inUse = false
            }, 10, TimeUnit.SECONDS)
        } catch (e: IOException) {
            LogUtils.e(e.toString())
            LogUtils.e("Network-->Failed to connect to address:${call?.request?.toAddress}")
            throw e
        }
        return this
    }

    @Throws(IOException::class)
    private fun connectSocket(call: Call?) {
        if (call?.request == null) return
        val rawSocket = SocketFactory.getDefault().createSocket()!!
        this.rawSocket = rawSocket
        var address = call.request!!.toAddress
        try {
            LogUtils.d("Network-->Connect to address:${address}")
            connectSocket(
                rawSocket, address,
                CONNECT_TIME_OUT
            )
        } catch (e: ConnectException) {
            LogUtils.e("Network-->Failed to connect to address:${address}:" + e)
            throw ConnectException("Failed to connect to $address").apply {
                initCause(e)
            }
        }
        try {
            source = rawSocket.source().buffer()
            sink = rawSocket.sink().buffer()
        } catch (npe: NullPointerException) {
            if (npe.message == NPE_THROW_WITH_NULL) {
                throw IOException(npe)
            }
        }
    }

    @Throws(IOException::class)
    private fun sendRequest(request: Request?, factory: Converter.Factory? = null) {
        request?.markInUse()
        val source = this.source!!
        val sink = this.sink!!
        codec = ExchangeCodec(source, sink)
        source.timeout().timeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
        sink.timeout().timeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
        var requestBody = factory?.requestBodyConverter()?.convert(request?.request)
        codec?.writeRequest(requestBody)
        codec?.finishRequest()
    }

    fun syncReadConnect(): String? {
        var result = codec?.syncReadConnect()
        inUse = false
        return result
    }

    @Throws(IOException::class)
    private fun connectSocket(
        socket: Socket,
        address: InetSocketAddress,
        connectTimeout: Int = CONNECT_TIME_OUT
    ) {
        try {
            socket.connect(address, connectTimeout)
        } catch (e: ClassCastException) {
            if (Build.VERSION.SDK_INT == 26) {
                throw IOException("Exception in connect", e)
            } else {
                throw e
            }
        }
    }

    companion object {
        private const val NPE_THROW_WITH_NULL = "throw with null exception"
        private var executor =
            Executors.newScheduledThreadPool(1, threadFactory("Dispatcher", false))
        var READ_TIME_OUT = 30 * 1000L
        var WRITE_TIME_OUT = 30 * 1000L
        var CONNECT_TIME_OUT = 30 * 1000
    }

}