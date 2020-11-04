package com.kincony.KControl.ui.adapter.device

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kincony.KControl.R
import com.kincony.KControl.net.data.Device
import com.kincony.KControl.utils.ImageLoader

class DimmerDeviceConvert(adapter: DeviceAdapter) : AbsBaseDeviceConvert(adapter) {

    override fun getLayoutId(): Int = R.layout.item_device_dimmer

    override fun convert(baseViewHolder: BaseViewHolder, device: Device) {
        val tvName = baseViewHolder.getView<TextView>(R.id.tv_name)
        val ivAdd = baseViewHolder.getView<ImageView>(R.id.iv_add);
        val ivSubtract = baseViewHolder.getView<ImageView>(R.id.iv_subtract);
        val seekBar = baseViewHolder.getView<AppCompatSeekBar>(R.id.seek_bar);
        val tvBrightness = baseViewHolder.getView<TextView>(R.id.tv_brightness)
        val tvAllBrightness = baseViewHolder.getView<TextView>(R.id.tv_all_brightness)
        if (device.type == 0) {
            ivAdd.visibility = if (device.isTouch || adapter.isSort) View.GONE else View.VISIBLE
            ivSubtract.visibility =
                if (device.isTouch || adapter.isSort) View.GONE else View.VISIBLE
            seekBar.visibility = if (!device.isTouch || adapter.isSort) View.GONE else View.VISIBLE
            tvBrightness.visibility = View.VISIBLE
            tvAllBrightness.visibility = View.GONE

            tvName.text = device.name
            tvBrightness.text = device.state
            ImageLoader.load(adapter.mContext, device.icon, baseViewHolder.getView(R.id.iv_icon))
            ivAdd.setOnClickListener {
                if (!TextUtils.isEmpty(device.state)) {
                    val newState = device.state.toInt() + 1
                    if (newState < 100) {
                        device.state = "$newState"
                        tvBrightness.text = device.state
                        updateAllChannelDeviceItem(device)
                        callback?.onAddClick(baseViewHolder.adapterPosition, device)
                    }
                }
            }
            ivSubtract.setOnClickListener {
                if (!TextUtils.isEmpty(device.state)) {
                    val newState = device.state.toInt() - 1
                    if (newState >= 0) {
                        device.state = "$newState"
                        tvBrightness.text = device.state
                        updateAllChannelDeviceItem(device)
                        callback?.onSubClick(baseViewHolder.adapterPosition, device)
                    }
                }
            }
            seekBar.max = 99
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                var isUserTouch = false

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (isUserTouch && !TextUtils.isEmpty(device.state) && progress >= 0 && progress < 100) {
                        device.state = "$progress"
                        tvBrightness.text = device.state
                        updateAllChannelDeviceItem(device)
                        callback?.onProgressChange(baseViewHolder.adapterPosition, device, progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isUserTouch = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isUserTouch = false
                }
            })
            if (!TextUtils.isEmpty(device.state)) {
                seekBar.progress = device.state.toInt()
            }

            tvName.setOnClickListener {
                callback?.onEditClick(baseViewHolder.adapterPosition, device)
            }
            baseViewHolder.itemView.setOnClickListener(null)
        } else if (device.type == 1) {
            ivAdd.visibility = View.GONE
            ivSubtract.visibility = View.GONE
            seekBar.visibility = View.GONE
            tvBrightness.visibility = View.GONE
            tvAllBrightness.visibility = View.VISIBLE

            tvName.text = device.name
            if (!TextUtils.isEmpty(device.itemName) && !TextUtils.isEmpty(device.state)) {
                val itemNameList = device.itemName.split(";")
                val stateList = device.state.split(",")
                var allBrightnessText = ""
                if (itemNameList.size == stateList.size) {
                    for ((index, name) in itemNameList.withIndex()) {
                        if (index != 0) allBrightnessText += "\n"
                        allBrightnessText += "${name}=${stateList[index]}"
                    }
                }
                tvAllBrightness.text = allBrightnessText
            } else {
                tvAllBrightness.text = ""
            }

            tvName.setOnClickListener(null)
            baseViewHolder.itemView.setOnClickListener() {
                callback?.onInPutEditClick(baseViewHolder.adapterPosition, device)
            }
        }
    }

    fun updateAllChannelDeviceItem(device: Device) {
        for ((position, d) in adapter.data.withIndex()) {
            if (d.type == 1 && d.addressId == device.addressId) {
                val stateList = d.state.split(",").toMutableList()
                var newState = ""
                if (device.number - 1 >= 0 && device.number - 1 < stateList.size) {
                    stateList[device.number - 1] = device.state
                    for ((index, state) in stateList.withIndex()) {
                        if (index != 0) newState += ","
                        newState += state
                    }
                }
                d.state = newState
                adapter.notifyItemChanged(position)
                break
            }
        }
    }

    var callback: Callback? = null

    interface Callback {
        fun onAddClick(position: Int, device: Device)

        fun onSubClick(position: Int, device: Device)

        fun onProgressChange(position: Int, device: Device, progress: Int)

        fun onEditClick(position: Int, device: Device)

        fun onInPutEditClick(position: Int, device: Device)
    }
}