package com.kincony.KControl.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kincony.KControl.App
import com.kincony.KControl.R

object SPUtils {
    val normalConfig by lazy {
        val spName = App.application.getString(R.string.key_sp)
        App.application.getSharedPreferences(spName, Context.MODE_PRIVATE)
    }
}