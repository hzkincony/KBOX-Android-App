package com.kincony.KControl.ui.adapter.device

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.net.data.Device
import com.kincony.KControl.net.data.DeviceType
import java.util.concurrent.Executors

class DeviceAdapter : BaseMultiItemQuickAdapter<Device, BaseViewHolder>() {
    var isSort = false

    companion object {
        internal var service = Executors.newScheduledThreadPool(1)
        internal var MAIN = Handler(Looper.getMainLooper())
    }

    val relayDeviceConvert: RelayDeviceConvert = RelayDeviceConvert(this)
    val dimmerDeviceConvert: DimmerDeviceConvert = DimmerDeviceConvert(this)
    val unknownDeviceConvert: UnknownDeviceConvert = UnknownDeviceConvert(this)

    init {
        addItemType(DeviceType.Unknown.value, unknownDeviceConvert.getLayoutId())
        addItemType(DeviceType.Relay_2.value, relayDeviceConvert.getLayoutId())
        addItemType(DeviceType.Relay_4.value, relayDeviceConvert.getLayoutId())
        addItemType(DeviceType.Relay_8.value, relayDeviceConvert.getLayoutId())
        addItemType(DeviceType.Relay_16.value, relayDeviceConvert.getLayoutId())
        addItemType(DeviceType.Relay_32.value, relayDeviceConvert.getLayoutId())
        addItemType(DeviceType.Dimmer_8.value, dimmerDeviceConvert.getLayoutId())
    }

    val mContext: Context
        get() = context

    override fun convert(baseViewHolder: BaseViewHolder, device: Device) {
        when (device.itemType) {
            DeviceType.Relay_2.value,
            DeviceType.Relay_4.value,
            DeviceType.Relay_8.value,
            DeviceType.Relay_16.value,
            DeviceType.Relay_32.value -> relayDeviceConvert.convert(baseViewHolder, device)
            DeviceType.Dimmer_8.value -> dimmerDeviceConvert.convert(baseViewHolder, device)
            else -> unknownDeviceConvert.convert(baseViewHolder, device)
        }
    }
}