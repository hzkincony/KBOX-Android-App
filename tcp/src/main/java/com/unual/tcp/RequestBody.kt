package com.unual.tcp

import okio.BufferedSink
import java.io.IOException

abstract class RequestBody() {

    open fun contentLength(): Long = -1L

    @Throws(IOException::class)
    abstract fun writeTo(sink: BufferedSink)
}