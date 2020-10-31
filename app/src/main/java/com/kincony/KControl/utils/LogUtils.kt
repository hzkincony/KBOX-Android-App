package com.kincony.KControl.utils

import android.util.Log

object LogUtils {
    val TAG: String = "KControl"

    var DEBUG: Boolean = true

    var FILE: Boolean = true

    fun d(vararg msgArray: String) {
        log(Log.DEBUG, TAG, *msgArray)
    }

    fun e(vararg msgArray: String) {
        log(Log.ERROR, TAG, *msgArray)
    }

    fun log(priority: Int, tag: String, vararg msgArray: String) {
        if (DEBUG) {
            for (msg in msgArray) {
                Log.println(priority, tag, msg)
            }
        }
    }
}