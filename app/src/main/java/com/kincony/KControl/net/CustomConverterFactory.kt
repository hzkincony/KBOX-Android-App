package com.kincony.KControl.net

import android.util.Log
import com.kincony.KControl.net.data.AlarmEvent
import com.kincony.KControl.net.data.KBoxState
import com.kincony.KControl.net.internal.converter.Converter
import com.kincony.KControl.net.internal.interfaces.ResponseBody
import org.greenrobot.eventbus.EventBus
import kotlin.concurrent.thread

class CustomConverterFactory private constructor() : Converter.Factory() {


    companion object {
        private var mInstance: CustomConverterFactory? = null;
        private var zero = asciiToString("0")

        private fun asciiToString(value: String): String {
            val sbu = StringBuffer()
            val chars = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in chars.indices) {
                sbu.append(Integer.parseInt(chars[i]).toChar())
            }
            return sbu.toString()
        }

        fun create(): CustomConverterFactory {
            return if (mInstance != null) {
                mInstance!!
            } else {
                synchronized(CustomConverterFactory::class.java) {
                    mInstance =
                        CustomConverterFactory()
                }
                mInstance!!
            }
        }
    }

    override fun requestBodyConverter(): Converter<String, String> {
        return CustomRequestConverter()
    }

    override fun responseBodyConverter(): Converter<String, ResponseBody> {
        return CustomResponseConverter()
    }

    class CustomRequestConverter :
        Converter<String, String> {
        override fun convert(value: String?): String? {
            Log.e("convert", "request:${value}")
            return value
        }
    }

    /**
     * 控制某一路继电器 SET
     *
     * 返回：RELAY-SET-255,x(字节继电器序号),x（1 字节动作 0/1）,OK/ERROR
     * 【例如 打开第 2 路， RELAY-SET-255,2,1】
     * 【 关闭第 2 路， RELAY-SET-255,2,0】
     */

    /**
     * 32 路控制盒：
     *  发送：RELAY-SET_ALL-255,D3,D2,D1,D0
     *  返回：RELAY-SET_ALL-255,D3,D2,D1,D0,OK/ERROR
     *  【例如 打开第 29-32 路， RELAY-SET_ALL-255,240,0,0,0】
     *  【 打开第 21-24 路， RELAY-SET_ALL-255,0,240,0,0】
     *
     * 16 路控制盒：
     *  发送：RELAY-SET_ALL-255,D1,D0
     *  返回：RELAY-SET_ALL-255,D1,D0,OK/ERROR
     *
     * 2/4/8 路控制盒：
     *  发送：RELAY-SET_ALL-255,D0
     *  返回：RELAY-SET_ALL-255,D0,OK/ERROR
     * 【例如 打开 1 3 5 7 这四路， RELAY-SET_ALL-255,85】
     * 【例如 打开 1 2 3 4 这四路， RELAY-SET_ALL-255,15】
     */

    /**
     * 返回：
     *  32 路：RELAY-STATE-255,D3,D2,D1,D0,OK/ERROR
     *  16 路：RELAY-STATE-255,D1,D0,OK/ERROR
     *  8 路：RELAY-STATE-255,D0,OK/ERROR
     */
    class CustomResponseConverter :
        Converter<String, ResponseBody> {
        override fun convert(valueString: String?): ResponseBody? {
            var value = valueString?.replace(zero, "")
            var result = value?.split(",")
            if (result == null || result.size <= 1) return null
            var type = result[0]//RELAY-STATE-255
            var succeed = result[result.size - 1]//OK
            var state = value?.replace("${type},", "")
                ?.replace(",${succeed}", "") ?: ""
            Log.e("convert", "response:${type},${state},${succeed}")
            checkMseage(type+state+succeed)
            return KBoxState(type, state, succeed == "OK")
        }

        /**
         * 检测接收信息
         * 读取到返回信息包含 ALARM时 触发获取输入端状态
         */
        fun checkMseage(valueString: String) {
            var flag: Boolean

            if (valueString == null) {
                flag = false
            } else {
                flag = valueString.contains("ALARM")
            }

            if (flag) {
                thread(start = true) {
                    Thread.sleep(100)
                    EventBus.getDefault().post(AlarmEvent("", 0))
                }
            }
        }
    }


}