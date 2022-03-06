package com.kincony.KControl.utils

import android.os.Build
import android.provider.Settings
import android.util.Log
import com.kincony.KControl.App
import java.io.File
import java.util.*

object LogUtils {
    val TAG: String = "LM"

    var DEBUG: Boolean = false

    var FILE: Boolean = false

    var isFirstWriteFile = true

    var logFile: File? = null;

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
        if (FILE && App.application != null) {
            if (logFile == null) {
                logFile = File(App.application.externalCacheDir, "log.txt")
            }
            if (!logFile!!.exists()) {
                logFile!!.createNewFile()
            }
            if (logFile!!.length() >= 10 * 1024 * 1024) {
                logFile!!.writeText("")
                isFirstWriteFile = true
            }
            if (logFile!!.length() == 0L) {
                logFile!!.appendText("LANGUAGE=${Locale.getDefault().language}\n")
                logFile!!.appendText("BRAND=${Build.BRAND}\n")
                logFile!!.appendText("MANUFACTURER=${Build.MANUFACTURER}\n")
                logFile!!.appendText("DEVICE=${Build.DEVICE}\n")
                logFile!!.appendText("MODEL=${Build.MODEL}\n")
                logFile!!.appendText("VERSION.SDK_INT=${Build.VERSION.SDK_INT}\n")

                logFile!!.appendText(
                        "ANDROID_ID=${Settings.System.getString(
                                App.application.contentResolver,
                                Settings.Secure.ANDROID_ID
                        )}\n"
                )
            }
            if (isFirstWriteFile) {
                logFile!!.appendText("==============================================================\n")
                isFirstWriteFile = false
            }
            for (msg in msgArray) {
                logFile!!.appendText("${getPriorityString(priority)}-${System.currentTimeMillis()}:$msg\n")
            }
        }
    }

    private fun getPriorityString(priority: Int): String = when (priority) {
        Log.DEBUG -> "D"
        Log.ERROR -> "E"
        else -> "V"
    }
}