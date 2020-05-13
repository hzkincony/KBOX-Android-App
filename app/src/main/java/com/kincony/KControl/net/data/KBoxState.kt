package com.kincony.KControl.net.data

import com.kincony.KControl.net.internal.interfaces.ResponseBody

public class KBoxState(
    @get:JvmName("type") var type: String,
    @get:JvmName("state") private var state: String,
    @get:JvmName("succeed") var succeed: Boolean
) :
    ResponseBody() {
    override fun isSucceed(): Boolean {
        return succeed && !state.isNullOrEmpty()
    }


    override fun body(): String {
        return state
    }
}