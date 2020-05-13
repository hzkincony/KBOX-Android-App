package com.kincony.KControl.utils

import com.kincony.KControl.net.data.Device
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.KBoxState

object KBoxStateRead {
    fun readOneState(kBoxState: KBoxState, device: Device) {
        when (kBoxState.type) {
            "RELAY-SET-255" -> {//设置一路
                var state = kBoxState.body()
                if (state.split(",").size == 2) {
                    var number = state.split(",")[0].toInt()
                    var open = state.split(",")[1] == "1"
                    if (number == device.number) {
                        device.open = open
                    }
                }
            }

            "RELAY-SET_ALL-255" -> {//设置所有

            }

            "RELAY-STATE-255" -> {//读取所有

            }
        }
    }

    fun readAllState(kBoxState: KBoxState, address: IPAddress, devices: List<Device>) {
        when (kBoxState.type) {
            "RELAY-SET-255" -> {//设置一路
            }
            "RELAY-GET_INPUT-255" -> {//设置一路
                var state = kBoxState.body()
                var deviceList = findDeviceByAddress(address, devices)
                if (state.length <= 3) {
                    var a:Int = state.toInt()
                    for (d in deviceList) {
                        if (d.type == 1)
                            d.body = a
                    }
                }


            }
            "RELAY-SET_ALL-255", "RELAY-STATE-255" -> {//设置所有||读取所有
                var state = kBoxState.body()
                var subArray = state.split(",")
                var deviceList = findDeviceByAddress(address, devices)
                when (subArray.size) {
                    1 -> {//2,4,8
                        if (subArray[0].length > 3) return
                        var r0 = Integer.valueOf(subArray[0])
                        for (d in deviceList) {
                            d.open = readIfOpen(r0, d.number)
                        }
                    }
                    2 -> {//16
                        if (subArray[0].length > 3) return
                        if (subArray[1].length > 3) return
                        var r0 = Integer.valueOf(subArray[0])
                        var r1 = Integer.valueOf(subArray[1])
                        for (d in deviceList) {
                            if (d.number > 8) {
                                d.open = readIfOpen(r0, d.number)
                            } else {
                                d.open = readIfOpen(r1, d.number)
                            }
                        }
                    }
                    4 -> {//32
                        if (subArray[0].length > 3) return
                        if (subArray[1].length > 3) return
                        if (subArray[2].length > 3) return
                        if (subArray[3].length > 3) return
                        var r0 = Integer.valueOf(subArray[0])
                        var r1 = Integer.valueOf(subArray[1])
                        var r2 = Integer.valueOf(subArray[2])
                        var r3 = Integer.valueOf(subArray[3])
                        for (d in deviceList) {
                            if (d.number > 24) {
                                d.open = readIfOpen(r0, d.number)
                            } else if (d.number > 16) {
                                d.open = readIfOpen(r1, d.number)
                            } else if (d.number > 8) {
                                d.open = readIfOpen(r2, d.number)
                            } else {
                                d.open = readIfOpen(r3, d.number)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun findDeviceByAddress(address: IPAddress, devices: List<Device>): List<Device> {
        var result = ArrayList<Device>()
        for (device in devices) {
            if (device.address == address) {
                result.add(device)
            }
        }
        return result
    }


    fun readIfOpen(number: Int, index: Int): Boolean {
        var tempIndex = index
        when (index) {
            in 1..8 -> {
                tempIndex = index
            }
            in 9..16 -> {
                tempIndex = index - 8
            }
            in 17..24 -> {
                tempIndex = index - 16
            }
            in 25..32 -> {
                tempIndex = index - 24
            }
        }
        var result = false
        when (tempIndex) {
            1 -> {
                result = (number and 0b00000001) == 0b00000001
            }
            2 -> {
                result = (number and 0b00000010) == 0b00000010
            }
            3 -> {
                result = (number and 0b00000100) == 0b00000100
            }
            4 -> {
                result = (number and 0b00001000) == 0b00001000
            }
            5 -> {
                result = (number and 0b00010000) == 0b00010000
            }
            6 -> {
                result = (number and 0b00100000) == 0b00100000
            }
            7 -> {
                result = (number and 0b01000000) == 0b01000000
            }
            8 -> {
                result = (number and 0b10000000) == 0b10000000
            }
        }
        return result
    }
}