package com.kincony.KControl.ui.adapter.device

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.Device

class UnknownDeviceConvert(adapter: DeviceAdapter) : AbsBaseDeviceConvert(adapter) {
    override fun getLayoutId(): Int = R.layout.item_device_unknonw

    override fun convert(baseViewHolder: BaseViewHolder, device: Device) {
        val unknownDeviceTip = adapter.mContext.getString(R.string.unknown_device)
        baseViewHolder.setText(R.id.tv_unknown, "${device.name} ${unknownDeviceTip} ${device.state}")
    }
}