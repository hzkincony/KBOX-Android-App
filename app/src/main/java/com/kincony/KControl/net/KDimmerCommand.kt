package com.kincony.KControl.net

/**
 * 以太网通信调光协议
 */
object KDimmerCommand : BaseCommand() {

    /**
     * 查询某一路当前亮度状态：0-99%
     * 如：
     * 发送 DIMMER-READ-1         1为第一路,
     * 接收 DIMMER-READ-53,OK     第一路亮度为53，如果失败的话，OK变成ERROR
     */
    fun readDimmer(number: Int): String = toRequestBody("DIMMER-READ-$number")

    /**
     * 一次性查询所有输出当前状态：0-99%
     * 如：
     * 发送 DIMMER-READ-ALL
     * 接收 DIMMER-READ-50,51,52,53,54,55,56,57,OK   后面分别是8路的当前亮度状态，如果失败的话，OK变成ERROR
     */
    fun readAllDimmer(): String = toRequestBody("DIMMER-READ-ALL")

    /**
     * 控制某一路输出亮度:0-99%
     * 如：
     * 发送 DIMMER-SEND-1,53      1为第一路,53为亮度53%的程度
     * 接收 DIMMER-SEND-1,53,OK   如果失败的话，OK变成ERROR
     */
    fun setDimmer(number: Int, brightness: Int): String {
        val b = if (brightness.toInt() < 10) "0${brightness}" else "$brightness"
        return toRequestBody("DIMMER-SEND-$number,$b")
    }

    /**
     * 一次性控制所有输出亮度：0-99%
     * 如：
     * 发送 DIMMER-SEND-ALL,50,51,52,53,54,55,56,57,58        后面分别是8路的亮度
     * 接收 DIMMER-SEND-ALL,50,51,52,53,54,55,56,57,58,OK     如果失败的话，OK变成ERROR
     */
    fun setAllDimmer(brightnessList: List<String>): String {
        val command = StringBuilder("DIMMER-SEND-ALL")
        for (brightness in brightnessList) {
            val b = if (brightness.toInt() < 10) "0${brightness}" else brightness
            command.append(",${b}")
        }
        return toRequestBody(command.toString())
    }
}