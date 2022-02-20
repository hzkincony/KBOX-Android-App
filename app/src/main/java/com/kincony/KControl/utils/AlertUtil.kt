package com.kincony.KControl.utils

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object AlertUtil {
    fun alert(
        activity: Activity,
        title: String?,
        message: String?,
        positiveText: String?,
        positiveListener: DialogInterface.OnClickListener?,
        negativeText: String?,
        negativeListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(activity)
        if (title != null) builder.setTitle(title)
        if (message != null) builder.setMessage(message)
        if (positiveText != null) builder.setPositiveButton(positiveText, positiveListener)
        if (negativeText != null) builder.setNegativeButton(negativeText, negativeListener)
        builder.show()
    }

    fun alert(
        activity: Activity,
        message: String,
        positiveText: String,
        positiveListener: DialogInterface.OnClickListener
    ) {
        alert(activity, null, message, positiveText, positiveListener, null, null)
    }
}