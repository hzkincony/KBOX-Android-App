package com.kincony.KControl.net

open class BaseCommand{
    protected fun toRequestBody(string: String) = string

//    private fun toRequestBody(string: String): RequestBody = object : RequestBody() {
//        override fun body(): String {
//            return string
//        }
//    }
}