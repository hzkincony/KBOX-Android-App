package com.kincony.KControl.ui.adapter

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.Device
import com.kincony.KControl.utils.ImageLoader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 设备适配器
 */
class DeviceAdapter : BaseQuickAdapter<Device, BaseViewHolder> {
    companion object {
        private var service = Executors.newScheduledThreadPool(1)
        private var MAIN = Handler(Looper.getMainLooper())
    }

    private var task: ScheduledFuture<*>? = null
    var callback: ((Device, Boolean) -> Unit)? = null
    var isSort = false

    constructor() : super(R.layout.item_device) {
        addChildClickViewIds(R.id.mSwitchClick)
        addChildLongClickViewIds(R.id.button)
    }

    override fun convert(helper: BaseViewHolder, item: Device) {

        when (item.type) {
            0 -> {
                helper.getView<LinearLayout>(R.id.root).visibility = View.VISIBLE
                helper.getView<LinearLayout>(R.id.root1).visibility = View.GONE
                helper.getView<SwitchCompat>(R.id.mSwitch).isChecked = item.open
                helper.getView<SwitchCompat>(R.id.mSwitch).isClickable = false

                helper.setGone(R.id.button, isSort || !item.isTouch)
                helper.setGone(R.id.mSwitchClick, isSort || item.isTouch)

                helper.setText(R.id.value, item.name)

                ImageLoader.load(context, item.icon, helper.getView(R.id.icon))
                ImageLoader.load(context, item.iconTouch, helper.getView(R.id.button))


                helper.getView<ImageView>(R.id.button).setOnTouchListener { v, event ->
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            task = service.schedule({
                                task = null
                                MAIN.post {
                                    callback?.invoke(item, true)
                                }
                            }, 250, TimeUnit.MILLISECONDS)
                            return@setOnTouchListener false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            return@setOnTouchListener false
                        }
                        MotionEvent.ACTION_UP -> {
                            task?.cancel(false)
                            if (task == null) {

                                service.schedule({
                                    MAIN.post {
                                        callback?.invoke(item, false)
                                    }
                                }, 200, TimeUnit.MILLISECONDS)

//                        callback?.invoke(item, false)
                            }
                            return@setOnTouchListener false
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            task?.cancel(false)
                            if (task == null) {

                                service.schedule({
                                    MAIN.post {
                                        callback?.invoke(item, false)
                                    }
                                }, 200, TimeUnit.MILLISECONDS)

//                        callback?.invoke(item, false)
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
            1 -> {
                helper.getView<LinearLayout>(R.id.root).visibility = View.GONE
                helper.getView<LinearLayout>(R.id.root1).visibility = View.VISIBLE
                helper.setText(R.id.value1, item.name)
                ImageLoader.load(context, item.icon, helper.getView(R.id.icon1))

                var itemName:List<String>
                if (item.itemName != null) {
                    itemName = item.itemName.split(";")
                    if (itemName.size == 0) {
                        helper.getView<View>(R.id.status1_1).visibility = View.INVISIBLE
                        helper.getView<View>(R.id.status1_2).visibility = View.INVISIBLE
                        helper.getView<View>(R.id.status1_3).visibility = View.INVISIBLE
                        helper.getView<View>(R.id.status1_4).visibility = View.INVISIBLE
                        helper.getView<View>(R.id.status1_5).visibility = View.INVISIBLE
                        helper.getView<View>(R.id.status1_6).visibility = View.INVISIBLE
                        helper.getView<View>(R.id.status1_7).visibility = View.INVISIBLE
                        helper.getView<View>(R.id.status1_8).visibility = View.INVISIBLE
                    } else if (itemName.size == 6) {
                        helper.getView<View>(R.id.status1_1).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_2).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_3).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_4).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_5).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_6).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_7).visibility = View.INVISIBLE
                        helper.getView<View>(R.id.status1_8).visibility = View.INVISIBLE
                        helper.setText(R.id.text1_1, itemName.get(0))
                        helper.setText(R.id.text1_2, itemName.get(1))
                        helper.setText(R.id.text1_3, itemName.get(2))
                        helper.setText(R.id.text1_4, itemName.get(3))
                        helper.setText(R.id.text1_5, itemName.get(4))
                        helper.setText(R.id.text1_6, itemName.get(5))
                    } else if (itemName.size == 8) {
                        helper.getView<View>(R.id.status1_1).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_2).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_3).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_4).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_5).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_6).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_7).visibility = View.VISIBLE
                        helper.getView<View>(R.id.status1_8).visibility = View.VISIBLE
                        helper.setText(R.id.text1_1, itemName.get(0))
                        helper.setText(R.id.text1_2, itemName.get(1))
                        helper.setText(R.id.text1_3, itemName.get(2))
                        helper.setText(R.id.text1_4, itemName.get(3))
                        helper.setText(R.id.text1_5, itemName.get(4))
                        helper.setText(R.id.text1_6, itemName.get(5))
                        helper.setText(R.id.text1_7, itemName.get(6))
                        helper.setText(R.id.text1_8, itemName.get(7))
                    }


                } else {
                    helper.getView<View>(R.id.status1_1).visibility = View.INVISIBLE
                    helper.getView<View>(R.id.status1_2).visibility = View.INVISIBLE
                    helper.getView<View>(R.id.status1_3).visibility = View.INVISIBLE
                    helper.getView<View>(R.id.status1_4).visibility = View.INVISIBLE
                    helper.getView<View>(R.id.status1_5).visibility = View.INVISIBLE
                    helper.getView<View>(R.id.status1_6).visibility = View.INVISIBLE
                    helper.getView<View>(R.id.status1_7).visibility = View.INVISIBLE
                    helper.getView<View>(R.id.status1_8).visibility = View.INVISIBLE
                }


                if (item.body and 0b00000001 == 0b00000001) {
                    helper.getView<View>(R.id.view1_1).setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    helper.getView<View>(R.id.view1_1).setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (item.body and 0b00000010 == 0b00000010) {
                    helper.getView<View>(R.id.view1_2).setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    helper.getView<View>(R.id.view1_2).setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (item.body and 0b00000100 == 0b00000100) {
                    helper.getView<View>(R.id.view1_3).setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    helper.getView<View>(R.id.view1_3).setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (item.body and 0b00001000 == 0b00001000) {
                    helper.getView<View>(R.id.view1_4).setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    helper.getView<View>(R.id.view1_4).setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (item.body and 0b00010000 == 0b00010000) {
                    helper.getView<View>(R.id.view1_5).setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    helper.getView<View>(R.id.view1_5).setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (item.body and 0b00100000 == 0b00100000) {
                    helper.getView<View>(R.id.view1_6).setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    helper.getView<View>(R.id.view1_6).setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (item.body and 0b01000000 == 0b01000000) {
                    helper.getView<View>(R.id.view1_7).setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    helper.getView<View>(R.id.view1_7).setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (item.body and 0b10000000 == 0b10000000) {
                    helper.getView<View>(R.id.view1_8).setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    helper.getView<View>(R.id.view1_8).setBackgroundResource(R.drawable.shape_circle_view_red)
                }

            }

        }



    }
}
