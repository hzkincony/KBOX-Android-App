package com.kincony.KControl.net.internal.interfaces

interface Callback {
    fun onStart()
    fun onFailure(e: Exception)
    fun onResponse(response: ResponseBody?)
}