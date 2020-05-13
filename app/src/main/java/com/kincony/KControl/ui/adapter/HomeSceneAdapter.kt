package com.kincony.KControl.ui.adapter

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.Scene
import com.kincony.KControl.utils.ImageLoader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 地址适配器
 */
class HomeSceneAdapter : BaseQuickAdapter<Scene, BaseViewHolder> {
    companion object {
        private var service = Executors.newScheduledThreadPool(1)
        private var MAIN = Handler(Looper.getMainLooper())
    }

    private var task: ScheduledFuture<*>? = null
    var callback: ((Scene, Boolean) -> Unit)? = null

    constructor() : super(R.layout.item_home_scene) {
        addChildClickViewIds(R.id.root)
        addChildLongClickViewIds(R.id.root)
    }

    override fun convert(helper: BaseViewHolder, item: Scene) {
        helper.setText(R.id.name, "${item.name}")
        ImageLoader.load(context, item.icon, helper.getView(R.id.icon))

        helper.getView<View>(R.id.root).setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (item.isTouch) {
                        task = service.schedule({
                            task = null
                            MAIN.post {
                                callback?.invoke(item, true)
                            }
                        }, 250, TimeUnit.MILLISECONDS)
                    }
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_MOVE -> {
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_UP -> {
                    if (item.isTouch) {
                        task?.cancel(false)
                        if (task == null) {
                            service.schedule({
                                MAIN.post {
                                    callback?.invoke(item, false)
                                }
                            }, 200, TimeUnit.MILLISECONDS)
//                            callback?.invoke(item, false)
                        }
                    }
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (item.isTouch) {
                        task?.cancel(false)
                        if (task == null) {
                            service.schedule({
                                MAIN.post {
                                    callback?.invoke(item, false)
                                }
                            }, 200, TimeUnit.MILLISECONDS)
//                            callback?.invoke(item, false)
                        }
                    }
                    return@setOnTouchListener false
                }
                else -> {
                    task?.cancel(false)
                    return@setOnTouchListener false
                }
            }
        }
    }
}