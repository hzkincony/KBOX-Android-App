package com.kincony.KControl.utils

import android.content.Context
import com.kincony.KControl.App
import com.kincony.KControl.R

object SPUtils {
    val normalConfig by lazy {
        val spName = App.application.getString(R.string.key_sp)
        App.application.getSharedPreferences(spName, Context.MODE_PRIVATE)
    }

    fun getTemperatureUnit(): String {
        return normalConfig.getString("temperature_unit", "℃")!!
    }

    fun setTemperatureUnit(unit: String) {
        normalConfig.edit().putString("temperature_unit", unit).commit()
    }
}