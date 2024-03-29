package com.kincony.KControl.net.usecase

import com.kincony.KControl.net.KDimmerCommand
import com.kincony.KControl.net.data.DeviceType
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.internal.Client
import com.kincony.KControl.net.internal.Request
import com.kincony.KControl.net.internal.interfaces.Callback

class KDimmerReadOneDimmerUseCase {
    /**
     * 读取1路调光器信息
     *
     * @param address ip地址信息
     * @param number 第几路
     * @param callback 回调
     */
    fun execute(address: IPAddress, number: Int, callback: Callback) {
        if (address.deviceType != DeviceType.Dimmer_8.value) throw IllegalArgumentException("设备类型不为：${DeviceType.Dimmer_8.value}(路调光器)")
        val request = Request.obtain(address.ip, address.port, KDimmerCommand.readDimmer(number))
        Client.newCall(request).enqueue(callback)
    }
}