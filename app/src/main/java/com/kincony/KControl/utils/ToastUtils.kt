package com.kincony.KControl.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import com.kincony.KControl.App

object ToastUtils {
    private val handler = Handler(Looper.getMainLooper())

    var toast: Toast? = null

    fun showToastShort(msg: String?) {
        showToast(msg, Toast.LENGTH_SHORT)
    }

    fun showToastLong(msg: String?) {
        showToast(msg, Toast.LENGTH_LONG)
    }

    private fun showToast(msg: String?, duration: Int) {
        if (TextUtils.isEmpty(msg)) return

        toast?.cancel()

        toast = Toast.makeText(App.application, msg, duration)

        if (Looper.getMainLooper() == Looper.myLooper()) {
            toast?.show()
        } else {
            handler.post { toast?.show() }
        }
    }

    fun destroy(activity: Activity) {
        toast?.cancel()
    }
}