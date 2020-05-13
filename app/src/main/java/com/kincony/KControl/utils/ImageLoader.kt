package com.kincony.KControl.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kincony.KControl.R

object ImageLoader {

    fun load(context: Context?, url: Int?, img: ImageView?) {
        if (context != null && img != null) {
            Glide.with(context).setDefaultRequestOptions(
                RequestOptions().centerCrop().placeholder(R.drawable.icon6).error(R.drawable.icon6)
            ).load(url).into(img)
        }
    }

    fun load(context: Context?, url: String?, img: ImageView?) {
        if (context != null && img != null) {
            Glide.with(context).setDefaultRequestOptions(
                RequestOptions().centerCrop().placeholder(R.drawable.icon6).error(R.drawable.icon6)
            ).load(url).into(img)
        }
    }

}