package com.kincony.KControl.net.internal

import com.kincony.KControl.net.internal.converter.Converter
import com.kincony.KControl.net.CustomConverterFactory

object Client {
    @get:JvmName("dispatcher")
    val dispatcher: Dispatcher = Dispatcher()

    fun newCall(request: Request, factory: Converter.Factory? = CustomConverterFactory.create()): Call = Call(this, request,factory)
}