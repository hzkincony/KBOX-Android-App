package com.kincony.KControl.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.ProtocolType

/**
 * 地址适配器
 */
class AddressAdapter : BaseQuickAdapter<IPAddress, BaseViewHolder> {

    constructor() : super(R.layout.item_address)

    override fun convert(helper: BaseViewHolder, item: IPAddress) {
        helper.setText(
            R.id.address,
            "${helper.itemView.context.getString(R.string.label_address)}${item.ip}:${item.port}"
        )
        helper.setText(
            R.id.deviceType,
            "${helper.itemView.context.getString(R.string.label_model)}${item.getDeviceTypeName(helper.itemView.context)}"
        )
        helper.setText(
            R.id.protocolType,
            "${helper.itemView.context.getString(R.string.label_protocol)}:${item.getProtocolTypeName()}"
        )
        if (ProtocolType.MQTT.value == item.protocolType) {
            helper.setVisible(R.id.deviceId, true)
            helper.setVisible(R.id.userName, true)
            helper.setText(
                R.id.deviceId,
                "${helper.itemView.context.getString(R.string.label_device_id)}:${item.deviceId}"
            )
            helper.setText(
                R.id.userName,
                "${helper.itemView.context.getString(R.string.label_user_name)}:${item.username}"
            )
        } else {
            helper.setGone(R.id.deviceId, true)
            helper.setGone(R.id.userName, true)
        }
    }

}