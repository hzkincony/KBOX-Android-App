package com.kincony.KControl.utils

import android.content.Context
import android.util.Log

object Tools {
    fun getAppVersionName(context: Context?): String? {
        var versionName: String? = ""
        try { // ---get the package info---
            val pi = context?.packageManager?.getPackageInfo(context?.packageName, 0)
            versionName = pi?.versionName
            var versioncode = pi?.versionCode
            if (versionName == null || versionName.isEmpty()) {
                return "${versioncode}"
            }
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }
        return versionName
    }
}