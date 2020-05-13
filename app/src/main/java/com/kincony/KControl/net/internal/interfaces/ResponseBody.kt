package com.kincony.KControl.net.internal.interfaces

abstract class ResponseBody {
    abstract fun isSucceed(): Boolean
    abstract fun body(): String

}