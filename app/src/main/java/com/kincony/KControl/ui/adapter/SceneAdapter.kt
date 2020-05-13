package com.kincony.KControl.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.Scene
import com.kincony.KControl.utils.ImageLoader

/**
 * 地址适配器
 */
class SceneAdapter : BaseQuickAdapter<Scene, BaseViewHolder> {

    constructor() : super(R.layout.item_scene) {

    }

    override fun convert(helper: BaseViewHolder, item: Scene) {
        helper.setText(R.id.name, "${item.name}")
        ImageLoader.load(context, item.icon, helper.getView(R.id.icon))
    }
}