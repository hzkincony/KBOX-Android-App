package com.kincony.KControl.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.kincony.KControl.net.data.DeviceType
import com.kincony.KControl.net.data.ProtocolType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object Tools {

    val gson: Gson = Gson()

    fun zip(str: String): String {
        var out: ByteArrayOutputStream? = null
        var zip: ZipOutputStream? = null

        try {
            out = ByteArrayOutputStream()
            zip = ZipOutputStream(out)
            zip.putNextEntry(ZipEntry("0"))
            zip.write(str.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            close(zip)
            close(out)
        }
        return str
    }

    fun unzip(str: String): String {
        var out: ByteArrayOutputStream? = null
        var input: ByteArrayInputStream? = null
        var zip: ZipInputStream? = null

        try {
            out = ByteArrayOutputStream()
            input = ByteArrayInputStream(Base64.decode(str.toByteArray(), Base64.DEFAULT))
            zip = ZipInputStream(input)
            zip.nextEntry
            val buffer = ByteArray(1024)
            var len: Int
            do {
                len = zip.read(buffer)
                if (len != -1) {
                    out.write(buffer, 0, len)
                }
            } while (len != -1)
            return out.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            close(zip)
            close(input)
            close(out)
        }
        return str
    }

    fun close(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAppVersionName(context: Context?): String? {
        var versionName: String? = ""
        try { // ---get the package info---
            val pi = context?.packageManager?.getPackageInfo(context?.packageName, 0)
            versionName = pi?.versionName
            var versioncode = pi?.versionCode
            if (versionName == null || versionName.isEmpty()) {
                return "${versioncode}"
            }
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }
        return versionName
    }

    fun getDeviceTypeEnum(deviceType: Int): DeviceType {
        return when (deviceType) {
            DeviceType.Relay_2.value -> DeviceType.Relay_2
            DeviceType.Relay_4.value -> DeviceType.Relay_4
            DeviceType.Relay_8.value -> DeviceType.Relay_8
            DeviceType.Relay_16.value -> DeviceType.Relay_16
            DeviceType.Relay_32.value -> DeviceType.Relay_32
            DeviceType.Dimmer_8.value -> DeviceType.Dimmer_8
            DeviceType.COLB.value -> DeviceType.COLB
            else -> DeviceType.Unknown
        }
    }

    fun getProtocolTypeEnum(protocolType: Int): ProtocolType {
        return when (protocolType) {
            ProtocolType.MQTT.value -> ProtocolType.MQTT
            else -> ProtocolType.TCP
        }
    }
}