package com.kincony.KControl.ui.adapter.device

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.Device
import com.kincony.KControl.utils.ImageLoader
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class RelayDeviceConvert(adapter: NewDeviceAdapter) : AbsBaseDeviceConvert(adapter) {
    private var task: ScheduledFuture<*>? = null
    var callback: ((Device, Boolean) -> Unit)? = null

    init {
        adapter.addChildClickViewIds(R.id.mSwitchClick)
        adapter.addChildLongClickViewIds(R.id.button)
    }

    override fun getLayoutId(): Int = R.layout.item_device

    override fun convert(baseViewHolder: BaseViewHolder, device: Device) {
        when (device.type) {
            0 -> {
                baseViewHolder.getView<LinearLayout>(R.id.root).visibility = View.VISIBLE
                baseViewHolder.getView<LinearLayout>(R.id.root1).visibility = View.GONE
                baseViewHolder.getView<SwitchCompat>(R.id.mSwitch).isChecked = device.open
                baseViewHolder.getView<SwitchCompat>(R.id.mSwitch).isClickable = false

                baseViewHolder.setGone(R.id.button, adapter.isSort || !device.isTouch)
                baseViewHolder.setGone(R.id.mSwitchClick, adapter.isSort || device.isTouch)

                baseViewHolder.setText(R.id.value, device.name)

                ImageLoader.load(adapter.mContext, device.icon, baseViewHolder.getView(R.id.icon))
                ImageLoader.load(
                    adapter.mContext,
                    device.iconTouch,
                    baseViewHolder.getView(R.id.button)
                )


                baseViewHolder.getView<ImageView>(R.id.button).setOnTouchListener { _, event ->
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            task = NewDeviceAdapter.service.schedule({
                                task = null
                                NewDeviceAdapter.MAIN.post {
                                    callback?.invoke(device, true)
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
                                NewDeviceAdapter.service.schedule({
                                    NewDeviceAdapter.MAIN.post {
                                        callback?.invoke(device, false)
                                    }
                                }, 200, TimeUnit.MILLISECONDS)
                            }
                            return@setOnTouchListener false
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            task?.cancel(false)
                            if (task == null) {

                                NewDeviceAdapter.service.schedule({
                                    NewDeviceAdapter.MAIN.post {
                                        callback?.invoke(device, false)
                                    }
                                }, 200, TimeUnit.MILLISECONDS)

//                        callback?.invoke(device, false)
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
                baseViewHolder.getView<LinearLayout>(R.id.root).visibility = View.GONE
                baseViewHolder.getView<LinearLayout>(R.id.root1).visibility = View.VISIBLE
                baseViewHolder.setText(R.id.value1, device.name)
                ImageLoader.load(adapter.mContext, device.icon, baseViewHolder.getView(R.id.icon1))

                val itemName: List<String>
                if (device.itemName != null) {
                    itemName = device.itemName.split(";")
                    if (itemName.size == 0) {
                        baseViewHolder.getView<View>(R.id.status1_1).visibility = View.INVISIBLE
                        baseViewHolder.getView<View>(R.id.status1_2).visibility = View.INVISIBLE
                        baseViewHolder.getView<View>(R.id.status1_3).visibility = View.INVISIBLE
                        baseViewHolder.getView<View>(R.id.status1_4).visibility = View.INVISIBLE
                        baseViewHolder.getView<View>(R.id.status1_5).visibility = View.INVISIBLE
                        baseViewHolder.getView<View>(R.id.status1_6).visibility = View.INVISIBLE
                        baseViewHolder.getView<View>(R.id.status1_7).visibility = View.INVISIBLE
                        baseViewHolder.getView<View>(R.id.status1_8).visibility = View.INVISIBLE
                    } else if (itemName.size == 6) {
                        baseViewHolder.getView<View>(R.id.status1_1).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_2).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_3).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_4).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_5).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_6).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_7).visibility = View.INVISIBLE
                        baseViewHolder.getView<View>(R.id.status1_8).visibility = View.INVISIBLE
                        baseViewHolder.setText(R.id.text1_1, itemName.get(0))
                        baseViewHolder.setText(R.id.text1_2, itemName.get(1))
                        baseViewHolder.setText(R.id.text1_3, itemName.get(2))
                        baseViewHolder.setText(R.id.text1_4, itemName.get(3))
                        baseViewHolder.setText(R.id.text1_5, itemName.get(4))
                        baseViewHolder.setText(R.id.text1_6, itemName.get(5))
                    } else if (itemName.size == 8) {
                        baseViewHolder.getView<View>(R.id.status1_1).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_2).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_3).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_4).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_5).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_6).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_7).visibility = View.VISIBLE
                        baseViewHolder.getView<View>(R.id.status1_8).visibility = View.VISIBLE
                        baseViewHolder.setText(R.id.text1_1, itemName.get(0))
                        baseViewHolder.setText(R.id.text1_2, itemName.get(1))
                        baseViewHolder.setText(R.id.text1_3, itemName.get(2))
                        baseViewHolder.setText(R.id.text1_4, itemName.get(3))
                        baseViewHolder.setText(R.id.text1_5, itemName.get(4))
                        baseViewHolder.setText(R.id.text1_6, itemName.get(5))
                        baseViewHolder.setText(R.id.text1_7, itemName.get(6))
                        baseViewHolder.setText(R.id.text1_8, itemName.get(7))
                    }
                } else {
                    baseViewHolder.getView<View>(R.id.status1_1).visibility = View.INVISIBLE
                    baseViewHolder.getView<View>(R.id.status1_2).visibility = View.INVISIBLE
                    baseViewHolder.getView<View>(R.id.status1_3).visibility = View.INVISIBLE
                    baseViewHolder.getView<View>(R.id.status1_4).visibility = View.INVISIBLE
                    baseViewHolder.getView<View>(R.id.status1_5).visibility = View.INVISIBLE
                    baseViewHolder.getView<View>(R.id.status1_6).visibility = View.INVISIBLE
                    baseViewHolder.getView<View>(R.id.status1_7).visibility = View.INVISIBLE
                    baseViewHolder.getView<View>(R.id.status1_8).visibility = View.INVISIBLE
                }


                if (device.body and 0b00000001 == 0b00000001) {
                    baseViewHolder.getView<View>(R.id.view1_1)
                        .setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    baseViewHolder.getView<View>(R.id.view1_1)
                        .setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (device.body and 0b00000010 == 0b00000010) {
                    baseViewHolder.getView<View>(R.id.view1_2)
                        .setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    baseViewHolder.getView<View>(R.id.view1_2)
                        .setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (device.body and 0b00000100 == 0b00000100) {
                    baseViewHolder.getView<View>(R.id.view1_3)
                        .setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    baseViewHolder.getView<View>(R.id.view1_3)
                        .setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (device.body and 0b00001000 == 0b00001000) {
                    baseViewHolder.getView<View>(R.id.view1_4)
                        .setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    baseViewHolder.getView<View>(R.id.view1_4)
                        .setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (device.body and 0b00010000 == 0b00010000) {
                    baseViewHolder.getView<View>(R.id.view1_5)
                        .setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    baseViewHolder.getView<View>(R.id.view1_5)
                        .setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (device.body and 0b00100000 == 0b00100000) {
                    baseViewHolder.getView<View>(R.id.view1_6)
                        .setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    baseViewHolder.getView<View>(R.id.view1_6)
                        .setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (device.body and 0b01000000 == 0b01000000) {
                    baseViewHolder.getView<View>(R.id.view1_7)
                        .setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    baseViewHolder.getView<View>(R.id.view1_7)
                        .setBackgroundResource(R.drawable.shape_circle_view_red)
                }

                if (device.body and 0b10000000 == 0b10000000) {
                    baseViewHolder.getView<View>(R.id.view1_8)
                        .setBackgroundResource(R.drawable.shape_circle_view_green)
                } else {
                    baseViewHolder.getView<View>(R.id.view1_8)
                        .setBackgroundResource(R.drawable.shape_circle_view_red)
                }

            }
        }
    }
}