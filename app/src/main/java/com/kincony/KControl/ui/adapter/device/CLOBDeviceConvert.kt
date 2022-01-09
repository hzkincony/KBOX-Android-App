package com.kincony.KControl.ui.adapter.device

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.Device
import com.kincony.KControl.utils.ImageLoader
import com.kincony.KControl.utils.SPUtils
import java.math.RoundingMode
import java.text.DecimalFormat

class CLOBDeviceConvert(adapter: DeviceAdapter) : AbsBaseDeviceConvert(adapter) {
    private var decimalFormat: DecimalFormat? = null

    override fun getLayoutId(): Int {
        decimalFormat = DecimalFormat("#0.00")
        decimalFormat!!.roundingMode = RoundingMode.HALF_UP
        return R.layout.item_device_clob
    }

    override fun convert(baseViewHolder: BaseViewHolder, device: Device) {
        val name = baseViewHolder.getView<TextView>(R.id.name)
        name.text = device.name

        val icon = baseViewHolder.getView<ImageView>(R.id.icon)
        ImageLoader.load(adapter.mContext, device.icon, icon)

        val llD = baseViewHolder.getView<LinearLayout>(R.id.ll_D)
        val llA = baseViewHolder.getView<LinearLayout>(R.id.ll_A)
        val llT = baseViewHolder.getView<LinearLayout>(R.id.ll_T)
        llD.visibility = View.GONE
        llA.visibility = View.GONE
        llT.visibility = View.GONE

        if (device.number in 1..4) {
            llD.visibility = View.VISIBLE
            val d1 = baseViewHolder.getView<View>(R.id.d1)
            val d2 = baseViewHolder.getView<View>(R.id.d2)
            val d3 = baseViewHolder.getView<View>(R.id.d3)
            val d4 = baseViewHolder.getView<View>(R.id.d4)
            val d1n = baseViewHolder.getView<TextView>(R.id.d1n)
            val d2n = baseViewHolder.getView<TextView>(R.id.d2n)
            val d3n = baseViewHolder.getView<TextView>(R.id.d3n)
            val d4n = baseViewHolder.getView<TextView>(R.id.d4n)
            val d = device.state.split(",")
            for ((index, value) in d.withIndex()) {
                when (index) {
                    0 -> {
                        d1.setBackgroundResource(if (value == "1") R.drawable.shape_circle_view_green else R.drawable.shape_circle_view_red)
                    }
                    1 -> {
                        d2.setBackgroundResource(if (value == "1") R.drawable.shape_circle_view_green else R.drawable.shape_circle_view_red)
                    }
                    2 -> {
                        d3.setBackgroundResource(if (value == "1") R.drawable.shape_circle_view_green else R.drawable.shape_circle_view_red)
                    }
                    3 -> {
                        d4.setBackgroundResource(if (value == "1") R.drawable.shape_circle_view_green else R.drawable.shape_circle_view_red)
                    }
                }
            }
            val n = device.itemName.split(";")
            for ((index, value) in n.withIndex()) {
                when (index) {
                    0 -> {
                        d1n.setText(value)
                    }
                    1 -> {
                        d2n.setText(value)
                    }
                    2 -> {
                        d3n.setText(value)
                    }
                    3 -> {
                        d4n.setText(value)
                    }
                }
            }
        } else if (device.number in 5..8) {
            llA.visibility = View.VISIBLE
            val a1 = baseViewHolder.getView<TextView>(R.id.a1)
            val a2 = baseViewHolder.getView<TextView>(R.id.a2)
            val a3 = baseViewHolder.getView<TextView>(R.id.a3)
            val a4 = baseViewHolder.getView<TextView>(R.id.a4)
            val a1n = baseViewHolder.getView<TextView>(R.id.a1n)
            val a2n = baseViewHolder.getView<TextView>(R.id.a2n)
            val a3n = baseViewHolder.getView<TextView>(R.id.a3n)
            val a4n = baseViewHolder.getView<TextView>(R.id.a4n)
            val d = device.state.split(",")
            val max = device.max.split(";")
            val min = device.min.split(";")
            val unit = device.unit.split(";")
            for ((index, value) in d.withIndex()) {
                val str = if (TextUtils.isEmpty(value)) {
                    value
                } else {
                    "${decimalFormat!!.format(value.toFloat() / 5.0 * (max[index].toFloat() - min[index].toFloat()) + min[index].toFloat())}${unit[index]}"
                }
                when (index) {
                    0 -> {
                        a1.setText(str)
                    }
                    1 -> {
                        a2.setText(str)
                    }
                    2 -> {
                        a3.setText(str)
                    }
                    3 -> {
                        a4.setText(str)
                    }
                }
            }
            val n = device.itemName.split(";")
            for ((index, value) in n.withIndex()) {
                when (index) {
                    0 -> {
                        a1n.setText(value)
                    }
                    1 -> {
                        a2n.setText(value)
                    }
                    2 -> {
                        a3n.setText(value)
                    }
                    3 -> {
                        a4n.setText(value)
                    }
                }
            }
        } else if (device.number == 9) {
            llT.visibility = View.VISIBLE
            val t1 = baseViewHolder.getView<TextView>(R.id.t1)
            val t2 = baseViewHolder.getView<TextView>(R.id.t2)
            val t3 = baseViewHolder.getView<TextView>(R.id.t3)
            val t4 = baseViewHolder.getView<TextView>(R.id.t4)
            val t5 = baseViewHolder.getView<TextView>(R.id.t5)
            val t1n = baseViewHolder.getView<TextView>(R.id.t1n)
            val t2n = baseViewHolder.getView<TextView>(R.id.t2n)
            val t3n = baseViewHolder.getView<TextView>(R.id.t3n)
            val t4n = baseViewHolder.getView<TextView>(R.id.t4n)
            val t5n = baseViewHolder.getView<TextView>(R.id.t5n)
            val t = device.state.split(",")
            for ((index, value) in t.withIndex()) {
                when (index) {
                    0 -> {
                        if (SPUtils.getTemperatureUnit() == "℉") {
                            t1.setText("${decimalFormat!!.format(value.toFloat() * 9.0 / 5.0 + 32.0)}℉")
                        } else {
                            t1.setText("$value℃")
                        }

                    }
                    1 -> {
                        if (SPUtils.getTemperatureUnit() == "℉") {
                            t2.setText("${decimalFormat!!.format(value.toFloat() * 9.0 / 5.0 + 32.0)}℉")
                        } else {
                            t2.setText("$value℃")
                        }
                    }
                    2 -> {
                        if (SPUtils.getTemperatureUnit() == "℉") {
                            t3.setText("${decimalFormat!!.format(value.toFloat() * 9.0 / 5.0 + 32.0)}℉")
                        } else {
                            t3.setText("$value℃")
                        }
                    }
                    3 -> {
                        if (SPUtils.getTemperatureUnit() == "℉") {
                            t4.setText("${decimalFormat!!.format(value.toFloat() * 9.0 / 5.0 + 32.0)}℉")
                        } else {
                            t4.setText("$value℃")
                        }
                    }
                    4 -> {
                        if (SPUtils.getTemperatureUnit() == "℉") {
                            t5.setText("${decimalFormat!!.format(value.toFloat() * 9.0 / 5.0 + 32.0)}℉")
                        } else {
                            t5.setText("$value℃")
                        }
                    }
                }
            }
            val n = device.itemName.split(";")
            for ((index, value) in n.withIndex()) {
                when (index) {
                    0 -> {
                        t1n.setText(value)
                    }
                    1 -> {
                        t2n.setText(value)
                    }
                    2 -> {
                        t3n.setText(value)
                    }
                    3 -> {
                        t4n.setText(value)
                    }
                    4 -> {
                        t5n.setText(value)
                    }
                }
            }
        }

        baseViewHolder.itemView.setOnClickListener {
            callback?.onInPutEditClick(baseViewHolder.adapterPosition, device)
        }
    }

    var callback: Callback? = null

    interface Callback {
        fun onInPutEditClick(position: Int, device: Device)
    }
}