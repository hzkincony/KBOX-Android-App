package com.kincony.KControl.net.internal.interfaces

abstract class RequestBody {
    abstract fun body(): String

    companion object {
        @JvmStatic
        @JvmName("create")
        fun String.toRequestBody(): RequestBody = object : RequestBody() {
            override fun body(): String {
                return this as String
            }
        }
    }
}