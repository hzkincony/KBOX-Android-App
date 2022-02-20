package com.kincony.KControl.net.internal.connection

import com.kincony.KControl.net.internal.Call
import com.kincony.KControl.net.internal.Request
import com.kincony.KControl.net.internal.converter.Converter
import com.kincony.KControl.net.internal.threadFactory
import com.kincony.KControl.utils.LogUtils
import okio.*
import java.io.IOException
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

    @Throws
    fun request(factory: Converter.Factory? = null): RealConnection {
        task?.cancel(false)
        if (rawSocket?.isConnected != true) {
            connectSocket(call)
        }
        sendRequest(call?.request, factory)
        task = executor.schedule({
            try {
                rawSocket?.close()
                com.kincony.KControl.utils.LogUtils.d("Network${call?.request?.toAddress}-->Close socket success")
            } catch (t: Throwable) {
                com.kincony.KControl.utils.LogUtils.e("Network/${call?.request?.ip}:${call?.request?.port}-->Failed to close socket")
            }
            call?.request?.clearUse()
            call?.request?.recycle()
            call?.request = null
            rawSocket = null
            source = null
            sink = null
            inUse = false
        }, AUTO_CLOSE_SOCKET_TIME_OUT, TimeUnit.MILLISECONDS)
        return this
    }

    @Throws(IOException::class)
    private fun connectSocket(call: Call?) {
        if (call?.request == null) return
        val rawSocket = SocketFactory.getDefault().createSocket()!!
        this.rawSocket = rawSocket
        val address = call.request!!.toAddress
        com.kincony.KControl.utils.LogUtils.d("Network${address}-->start connect to address")
        connectSocket(
            rawSocket, address,
            CONNECT_TIME_OUT
        )
        source = rawSocket.source().buffer()
        sink = rawSocket.sink().buffer()
    }

    @Throws(IOException::class)
    private fun sendRequest(request: Request?, factory: Converter.Factory? = null) {
        request?.markInUse()
        val source = this.source!!
        val sink = this.sink!!
        codec = ExchangeCodec(source, sink)
        source.timeout().timeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
        sink.timeout().timeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
        var requestBody =
            factory?.requestBodyConverter()?.convert(request?.toAddress, request?.request)
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
        socket.connect(address, connectTimeout)
    }

    companion object {
        private const val NPE_THROW_WITH_NULL = "throw with null exception"
        private var executor =
            Executors.newScheduledThreadPool(1, threadFactory("Dispatcher", false))
        var READ_TIME_OUT = 30 * 1000L
        var WRITE_TIME_OUT = 30 * 1000L
        var CONNECT_TIME_OUT = 5 * 1000
        var AUTO_CLOSE_SOCKET_TIME_OUT =
            CONNECT_TIME_OUT + READ_TIME_OUT + WRITE_TIME_OUT + 1000
    }

}