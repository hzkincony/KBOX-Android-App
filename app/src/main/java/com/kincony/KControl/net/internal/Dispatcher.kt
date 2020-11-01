package com.kincony.KControl.net.internal

import android.os.Handler
import android.os.Looper
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class Dispatcher() {
    companion object {
        var MAIN = Handler(Looper.getMainLooper())
    }

    @get:Synchronized
    var maxRequests = 1
        set(maxRequests) {
            require(maxRequests >= 1) { "max < 1: $maxRequests" }
            synchronized(this) {
                field = maxRequests
            }
            promoteAndExecute()
        }

    private var executorServiceOrNull: ExecutorService? = null

    @get:Synchronized
    @get:JvmName("executorService")
    val executorService: ExecutorService
        get() {
            if (executorServiceOrNull == null) {
                executorServiceOrNull = ThreadPoolExecutor(
                    0, Int.MAX_VALUE, 60, TimeUnit.SECONDS,
                    SynchronousQueue(),
                    threadFactory("Dispatcher", false)
                )
            }
            return executorServiceOrNull!!
        }


    private val readyAsyncCalls = ArrayDeque<Call.AsyncCall>()

    private val runningAsyncCalls = ArrayDeque<Call.AsyncCall>()

    internal fun enqueue(call: Call.AsyncCall) {
        synchronized(this) {
            readyAsyncCalls.add(call)
        }
        promoteAndExecute()
    }

    private fun promoteAndExecute(): Boolean {
        this.assertThreadDoesntHoldLock()
        val executableCalls = mutableListOf<Call.AsyncCall>()
        val isRunning: Boolean
        synchronized(this) {
            val i = readyAsyncCalls.iterator()
            while (i.hasNext()) {
                val asyncCall = i.next()

                if (runningAsyncCalls.size >= this.maxRequests) break // Max capacity.

                i.remove()
                executableCalls.add(asyncCall)
                runningAsyncCalls.add(asyncCall)
            }
            isRunning = runningAsyncCalls.size > 0
        }

        for (i in 0 until executableCalls.size) {
            val asyncCall = executableCalls[i]
            asyncCall.executeOn(executorService)
        }

        return isRunning
    }

    internal fun finished(call: Call.AsyncCall) {
        finished(runningAsyncCalls, call)
    }

    private fun <T> finished(calls: Deque<T>, call: T) {
        synchronized(this) {
            if (!calls.remove(call)) throw AssertionError("Call wasn't in-flight!")
        }
        promoteAndExecute()
    }
}
