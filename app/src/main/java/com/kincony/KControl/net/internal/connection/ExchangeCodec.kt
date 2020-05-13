/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kincony.KControl.net.internal.connection

import com.kincony.KControl.net.internal.skipAll
import java.io.IOException
import java.util.concurrent.TimeUnit.MILLISECONDS
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.ForwardingTimeout
import okio.Source
import okio.Timeout

class ExchangeCodec(
    private val source: BufferedSource,
    private val sink: BufferedSink
) {

    fun finishRequest() {
        sink.flush()
    }


    fun writeRequest(requestLine: String?) {
        sink.writeUtf8(requestLine ?: "")
    }

    fun skipAll() {
        val body = newUnknownLengthSource()
        body.skipAll(Int.MAX_VALUE, MILLISECONDS)
        body.close()
    }


    fun readConnect(buffer: Buffer) {
        val body = newUnknownLengthSource()
        body.readIntoBuffer(buffer)
        body.close()
    }


    fun syncReadConnect(): String {
        val body = newUnknownLengthSource()
        var result = body.syncRead()
        body.close()
        return result
    }

    private fun newUnknownLengthSource(): Source {
        return UnknownLengthSource()
    }

    fun detachTimeout(timeout: ForwardingTimeout) {
        val oldDelegate = timeout.delegate
        timeout.setDelegate(Timeout.NONE)
        oldDelegate.clearDeadline()
        oldDelegate.clearTimeout()
    }

    abstract inner class AbstractSource : Source {
        private val timeout = ForwardingTimeout(source.timeout())
        protected var closed: Boolean = false

        override fun timeout(): Timeout = timeout

        override fun read(sink: Buffer, byteCount: Long): Long {
            return try {
                source.read(sink, byteCount)
            } catch (e: IOException) {
                responseBodyComplete()
                throw e
            }
        }

        internal fun responseBodyComplete() {
            detachTimeout(timeout)
        }
    }

    inner class UnknownLengthSource : AbstractSource() {
        private var inputExhausted: Boolean = false

        override fun read(sink: Buffer, byteCount: Long): Long {
            require(byteCount >= 0L) { "byteCount < 0: $byteCount" }
            check(!closed) { "closed" }
            if (inputExhausted) return -1
            return super.read(sink, byteCount)
        }

        override fun close() {
            if (closed) return
            if (!inputExhausted) {
                responseBodyComplete()
            }
            closed = true
        }
    }

    fun Source.readIntoBuffer(buffer: Buffer): Unit {
        do {
            var length = read(buffer, 8192)
            var byteString = buffer.readByteString(length)
            var s = byteString.string(Charsets.UTF_8)
        } while (length != 0L)
    }

    private fun Source.syncRead(): String {
        var buffer = Buffer()
        var length = read(buffer, 8192)
        var byteString = buffer.readByteString(length)
        return byteString.string(Charsets.UTF_8)
    }
}





