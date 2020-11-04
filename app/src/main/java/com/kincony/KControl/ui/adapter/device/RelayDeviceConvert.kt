package com.kincony.KControl.ui.adapter.device

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.Device
import com.kincony.KControl.utils.ImageLoader
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class RelayDeviceConvert(adapter: DeviceAdapter) : AbsBaseDeviceConvert(adapter) {
    private var task: ScheduledFuture<*>? = null

    override fun getLayoutId(): Int = R.layout.item_device

    override fun convert(baseViewHolder: BaseViewHolder, device: Device) {
        val root = baseViewHolder.getView<LinearLayout>(R.id.root)
        val root1 = baseViewHolder.getView<LinearLayout>(R.id.root1)
        val mSwitch = baseViewHolder.getView<SwitchCompat>(R.id.mSwitch)
        val button = baseViewHolder.getView<AppCompatImageView>(R.id.button)
        val mSwitchClick = baseViewHolder.getView<LinearLayout>(R.id.mSwitchClick)
        val value = baseViewHolder.getView<TextView>(R.id.value)
        val value1 = baseViewHolder.getView<TextView>(R.id.value1)
        val icon = baseViewHolder.getView<ImageView>(R.id.icon)
        val icon1 = baseViewHolder.getView<ImageView>(R.id.icon1)

        when (device.type) {
            0 -> {
                root.visibility = View.VISIBLE
                root1.visibility = View.GONE
                mSwitch.isChecked = device.open
                mSwitch.isClickable = false

                button.visibility =
                    if (adapter.isSort || !device.isTouch) View.GONE else View.VISIBLE
                mSwitchClick.visibility =
                    if (adapter.isSort || device.isTouch) View.GONE else View.VISIBLE

                value.text = device.name

                ImageLoader.load(adapter.mContext, device.icon, icon)
                ImageLoader.load(adapter.mContext, device.iconTouch, button)

                mSwitchClick.setOnClickListener() {
                    device.open = !device.open
                    baseViewHolder.getView<SwitchCompat>(R.id.mSwitch).isChecked = device.open
                    callback?.onSwitchClick(baseViewHolder.adapterPosition, device)
                }

                button.setOnTouchListener { _, event ->
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            task = DeviceAdapter.service.schedule({
                                task = null
                                DeviceAdapter.MAIN.post {
                                    callback?.onSceneActionDown(
                                        baseViewHolder.adapterPosition,
                                        device
                                    )
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
                                DeviceAdapter.service.schedule({
                                    DeviceAdapter.MAIN.post {
                                        callback?.onSceneActionUp(
                                            baseViewHolder.adapterPosition,
                                            device
                                        )
                                    }
                                }, 250, TimeUnit.MILLISECONDS)
                            }
                            return@setOnTouchListener false
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            task?.cancel(false)
                            if (task == null) {

                                DeviceAdapter.service.schedule({
                                    DeviceAdapter.MAIN.post {
                                        callback?.onSceneActionUp(
                                            baseViewHolder.adapterPosition,
                                            device
                                        )
                                    }
                                }, 250, TimeUnit.MILLISECONDS)

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
                value.setOnClickListener {
                    callback?.onEditClick(baseViewHolder.adapterPosition, device)
                }
                baseViewHolder.itemView.setOnClickListener(null)
            }
            1 -> {
                root.visibility = View.GONE
                root1.visibility = View.VISIBLE
                value1.text = device.name
                ImageLoader.load(adapter.mContext, device.icon, icon1)

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
                baseViewHolder.itemView.setOnClickListener() {
                    callback?.onInPutEditClick(baseViewHolder.adapterPosition, device)
                }
            }
        }
    }

    var callback: Callback? = null

    interface Callback {
        fun onSceneActionDown(position: Int, device: Device)

        fun onSceneActionUp(position: Int, device: Device)

        fun onSwitchClick(position: Int, device: Device)

        fun onEditClick(position: Int, device: Device)

        fun onInPutEditClick(position: Int, device: Device)
    }
}