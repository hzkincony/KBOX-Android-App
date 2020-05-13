package com.kincony.KControl.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.IPAddress

/**
 * 地址适配器
 */
class AddressAdapter : BaseQuickAdapter<IPAddress, BaseViewHolder> {

    constructor() : super(R.layout.item_address)

    override fun convert(helper: BaseViewHolder, item: IPAddress) {
        helper.setText(R.id.ip, "IP:${item.ip}")
        helper.setText(R.id.port, "Port:${item.port}")
    }
}