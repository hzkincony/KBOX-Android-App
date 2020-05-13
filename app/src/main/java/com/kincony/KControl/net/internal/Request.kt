package com.kincony.KControl.net.internal

import android.util.Log
import com.kincony.KControl.utils.IPUtils.isIp
import java.net.InetAddress
import java.net.InetSocketAddress

class Request {
    var ip: String = "0.0.0.0"
    var request: String? = null
    var port: Int = 0
    private var next: Request? = null
    private var flags = 0
    private var addressOrNull: InetSocketAddress? = null

    @get:Synchronized
    @get:JvmName("toAddress")
    val toAddress: InetSocketAddress
        get() {
            if (addressOrNull == null) {
                var host: String? = ip
                try {
                    if (!isIp(ip)) {
                        var result = InetAddress.getAllByName(host).toList()
                        for (i in result) {
                            host = "${result[0]}".replace("${host}/", "")
                        }
                    }
                    Log.e("TAG", "host_${host}")
                } catch (e: Exception) {
                    throw e
                }
                addressOrNull = InetSocketAddress(host, port)
            }
            return addressOrNull!!
        }


    private constructor(ip: String, port: Int, request: String) {
        this.request = request
        this.port = port
        this.ip = ip
    }

    companion object {
        private val sPoolSync = Any()
        private var sPool: Request? = null
        private var sPoolSize = 0
        private const val MAX_POOL_SIZE = 50
        private const val gCheckRecycle = true
        const val FLAG_IN_USE = 1 shl 0

        fun obtain(ip: String, port: Int, request: String): Request {
            synchronized(sPoolSync) {
                if (sPool != null) {
                    val m: Request = sPool!!
                    sPool = m?.next
                    m?.next = null
                    m?.flags = 0
                    m?.ip = ip
                    m?.port = port
                    m?.request = request
                    sPoolSize--
                    return m
                }
            }
            return Request(ip, port, request)
        }
    }

    fun markInUse() {
        flags = flags or FLAG_IN_USE
    }

    fun clearUse() {
        request = ""
        flags = 0
    }

    private fun isInUse(): Boolean {
        return flags and FLAG_IN_USE == FLAG_IN_USE
    }


    fun recycle() {
        if (isInUse()) {
            check(!gCheckRecycle) {
                ("This request cannot be recycled because it is still in use.")
            }
            return
        }
        recycleUnchecked()
    }

    private fun recycleUnchecked() {
        flags = FLAG_IN_USE
        synchronized(sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool
                sPool = this
                sPoolSize++
            }
        }
    }
}