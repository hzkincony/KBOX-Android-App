package com.kincony.KControl.net.usecase

import com.kincony.KControl.net.KDimmerCommand
import com.kincony.KControl.net.data.DeviceType
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.internal.Client
import com.kincony.KControl.net.internal.Request
import com.kincony.KControl.net.internal.interfaces.Callback

class KDimmerSetAllDimmerUseCase {
    /**
     * 设置多路调光器信息
     *
     * @param address ip地址信息
     * @param brightness 亮度
     * @param callback 回调
     */
    fun execute(address: IPAddress, brightnessList: List<String>, callback: Callback) {
        if (address.type != DeviceType.Dimmer_8.value) throw IllegalArgumentException("设备类型不为：${DeviceType.Dimmer_8.value}(路调光器)")

        val request =
            Request.obtain(
                address.ip,
                address.port,
                KDimmerCommand.setAllDimmer(brightnessList)
            )
        Client.newCall(request).enqueue(callback)
    }
}