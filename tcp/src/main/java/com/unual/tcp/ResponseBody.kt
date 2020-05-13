package com.unual.tcp

import okio.BufferedSource

abstract class ResponseBody {
    abstract fun contentLength(): Long

    abstract fun source(): BufferedSource
}