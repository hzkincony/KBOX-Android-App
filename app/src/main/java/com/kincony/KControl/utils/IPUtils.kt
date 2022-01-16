package com.kincony.KControl.utils

import com.kincony.KControl.net.data.DeviceType
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.ProtocolType
import java.util.regex.Pattern

object IPUtils {

    /** * 判断是否为合法IP * @return the ip */
    fun isIp(ipAddress: String): Boolean {
        val ip =
                "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"
        val pattern = Pattern.compile(ip)
        val matcher = pattern.matcher(ipAddress)
        return matcher.matches()
    }

    fun getDefaultName(address: IPAddress, number: Int): String {
        var result = ""
        var preName = if (address.ip.length > 4) address.ip.substring(address.ip.length - 4) else address.ip
        preName += ":${address.port}"
        preName += if (address.protocolType == ProtocolType.MQTT.value) {
            if (address.deviceId!!.length > 4) {
                ":${address.deviceId!!.substring(address.deviceId!!.length - 4)}"
            } else {
                ":${address.deviceId}"
            }
        } else {
            ""
        }
        if (address.deviceType == DeviceType.COLB.value) {
            when (number) {
                1 -> result = "${preName}_D1-D4"
                2 -> result = "${preName}_D5-D8"
                3 -> result = "${preName}_D9-D12"
                4 -> result = "${preName}_D13-D16"
                5 -> result = "${preName}_A1-D4"
                6 -> result = "${preName}_A5-A8"
                7 -> result = "${preName}_A9-A12"
                8 -> result = "${preName}_A13-A16"
                9 -> result = "${preName}_T1-T5"
            }
        } else {
            result = "${preName}_${number}"
        }
        return result
    }
}