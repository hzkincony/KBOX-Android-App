package com.kincony.KControl.net.data

class RefreshAddressEvent(val address: IPAddress?)
class RefreshSceneEvent()

class DeviceChange(
    var id: Int,
    var name: String,
    var icon: Int,
    var mode: Boolean = false,
    var iconTouch: Int = 0
)

class DeviceInPutChange(
    var id: Int,
    var name: String,
    var itemName: String,
    var icon: Int,
    var max: String,
    var min: String,
    var unit: String
)

class IconEvent(var icon: Int, var code: Int)

class AlarmEvent(var ip: String, var port: Int)

class UpdateDeviceUI(var ipAddress: IPAddress? = null)