package com.kincony.KControl.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.utils.ImageLoader

/**
 * 图标适配器
 */
class IconAdapter : BaseQuickAdapter<Int, BaseViewHolder> {

    constructor() : super(R.layout.item_icon)

    override fun convert(helper: BaseViewHolder, item: Int) {
        ImageLoader.load(context, item, helper.getView(R.id.icon))
    }
}