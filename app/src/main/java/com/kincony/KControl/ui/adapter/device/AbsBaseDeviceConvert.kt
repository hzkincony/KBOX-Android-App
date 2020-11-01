package com.kincony.KControl.ui.adapter.device

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.net.data.Device

abstract class AbsBaseDeviceConvert(protected val adapter: NewDeviceAdapter) {
    abstract fun getLayoutId(): Int

    abstract fun convert(baseViewHolder: BaseViewHolder, device: Device)
}