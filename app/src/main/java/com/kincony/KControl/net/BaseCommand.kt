package com.kincony.KControl.net

open class BaseCommand {
    protected fun toRequestBody(string: String) = string + ",OK"
}