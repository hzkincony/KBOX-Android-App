package com.kincony.KControl.net

object KBoxCommand {

    /**
     * 控制某一路继电器 SET
     * 返回：RELAY-SET-255,x(字节继电器序号),x（1 字节动作 0/1）,OK/ERROR
     * 【例如 打开第 2 路， RELAY-SET-255,2,1】
     * 【 关闭第 2 路， RELAY-SET-255,2,0】
     */
    fun setState(number: Int, action: Int): String {
        return toRequestBody("RELAY-SET-255,$number,$action")
    }

    /**
     * 32 路控制盒：
     *  发送：RELAY-SET_ALL-255,D3,D2,D1,D0
     *  返回：RELAY-SET_ALL-255,D3,D2,D1,D0,OK/ERROR
     *  【例如 打开第 29-32 路， RELAY-SET_ALL-255,240,0,0,0】
     *  【 打开第 21-24 路， RELAY-SET_ALL-255,0,240,0,0】
     * 16 路控制盒：
     *  发送：RELAY-SET_ALL-255,D1,D0
     *  返回：RELAY-SET_ALL-255,D1,D0,OK/ERROR
     * 2/4/8 路控制盒：
     *  发送：RELAY-SET_ALL-255,D0
     *  返回：RELAY-SET_ALL-255,D0,OK/ERROR
     * 【例如 打开 1 3 5 7 这四路， RELAY-SET_ALL-255,85】
     * 【例如 打开 1 2 3 4 这四路， RELAY-SET_ALL-255,15】
     */
    fun setAllState(action: String): String {
        return toRequestBody("RELAY-SET_ALL-255,${action}")
    }

    /**
     * 返回：
     *  32 路：RELAY-STATE-255,D3,D2,D1,D0,OK/ERROR
     *  16 路：RELAY-STATE-255,D1,D0,OK/ERROR
     *  8 路：RELAY-STATE-255,D0,OK/ERROR
     */
    fun readAllState(): String {
        return toRequestBody("RELAY-STATE-255")
    }


    /**
     * 查询某一路继电器状态 READ
     * 返回：RELAY-READ-255,x(字节继电器序号),x(1 字节状态 0/1),OK/ERROR
     *【例如 查询第 3 路的继电器状态， RELAY-READ-255,3】
     */
    fun readState(number: Int): String {
        return toRequestBody("RELAY-READ-255,$number")
    }

    /**
     * 查询输入状态 GET_INPUT：
     * 返回：RELAY-GET_INPUT-255,x(1 字节状态),OK/ERROR
     */
    fun getState(): String {
        return toRequestBody("RELAY-GET_INPUT-255")
    }


    /**
     * 触发报警 ALARM：
     * 服务器返回：RELAY-ALARM-x(1 字节报警通道),OK/ERROR
     */
    fun alarmState(number: Int): String {
        return toRequestBody("RELAY-ALARM-$number")
    }

    private fun toRequestBody(string: String) = string

//    private fun toRequestBody(string: String): RequestBody = object : RequestBody() {
//        override fun body(): String {
//            return string
//        }
//    }

}