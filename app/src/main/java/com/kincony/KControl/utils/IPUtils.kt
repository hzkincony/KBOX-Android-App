package com.kincony.KControl.utils

import com.kincony.KControl.net.data.DeviceType
import com.kincony.KControl.net.data.IPAddress
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
        if (isIp(address.ip)) {
            val ipArray = address.ip.split(".")
            if (ipArray.size != 4) {
                result = "null:${address.port}_${number}"
            } else {
                if (address.deviceType == DeviceType.COLB.value) {
                    when (number) {
                        1 -> result = "${ipArray[3]}:${address.port}_D1-D4"
                        2 -> result = "${ipArray[3]}:${address.port}_D5-D8"
                        3 -> result = "${ipArray[3]}:${address.port}_D9-D12"
                        4 -> result = "${ipArray[3]}:${address.port}_D13-D16"
                        5 -> result = "${ipArray[3]}:${address.port}_A1-D4"
                        6 -> result = "${ipArray[3]}:${address.port}_A5-A8"
                        7 -> result = "${ipArray[3]}:${address.port}_A9-A12"
                        8 -> result = "${ipArray[3]}:${address.port}_A13-A16"
                        9 -> result = "${ipArray[3]}:${address.port}_T1-T5"
                    }
                } else {
                    result = "${ipArray[3]}:${address.port}_${number}"
                }
            }
        } else {
            result = "${address.ip}:${address.port}_${number}"
        }
        return result
    }
}