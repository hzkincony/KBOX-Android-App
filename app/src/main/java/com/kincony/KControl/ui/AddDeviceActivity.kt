package com.kincony.KControl.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.kincony.KControl.R
import com.kincony.KControl.net.data.DeviceType
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.ProtocolType
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.ui.scan.ScanActivity
import com.kincony.KControl.utils.Tools
import kotlinx.android.synthetic.main.activity_add_device.*
import org.json.JSONObject

class AddDeviceActivity : BaseActivity() {

    /**
     * 当前添加设备弹窗选中的设备类型，默认选中2路继电器
     */
    private var selectedDeviceType: Int = DeviceType.Relay_2.value

    /**
     * 当前添加设备弹窗选中的设备协议类型，默认自定义TCP
     */
    private var selectedProtocolType: Int = ProtocolType.TCP.value

    private val onItemSelectedListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        if (selectedDeviceType == DeviceType.CAMERA.value) {
                            selectedProtocolType = ProtocolType.CAMERA.value
                            showCAMERAInput()
                        } else {
                            selectedProtocolType = ProtocolType.TCP.value
                            showTCPInput()
                        }
                    }
                    1 -> {
                        selectedProtocolType = ProtocolType.MQTT.value
                        showMQTTInput()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    override fun getLayoutId(): Int {
        return R.layout.activity_add_device
    }

    override fun initView() {
        selectedDeviceType = DeviceType.Relay_2.value

        model.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        if (selectedDeviceType == DeviceType.CAMERA.value) {
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocol)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                            protocol.setSelection(0)
                        }
                        selectedDeviceType = DeviceType.Relay_2.value
                        protocol.isEnabled = true
                    }
                    1 -> {
                        if (selectedDeviceType == DeviceType.CAMERA.value) {
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocol)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                            protocol.setSelection(0)
                        }
                        selectedDeviceType = DeviceType.Relay_4.value
                        protocol.isEnabled = true
                    }
                    2 -> {
                        if (selectedDeviceType == DeviceType.CAMERA.value) {
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocol)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                            protocol.setSelection(0)
                        }
                        selectedDeviceType = DeviceType.Relay_8.value
                        protocol.isEnabled = true
                    }
                    3 -> {
                        if (selectedDeviceType == DeviceType.CAMERA.value) {
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocol)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                            protocol.setSelection(0)
                        }
                        selectedDeviceType = DeviceType.Relay_16.value
                        protocol.isEnabled = true
                    }
                    4 -> {
                        if (selectedDeviceType == DeviceType.CAMERA.value) {
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocol)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                            protocol.setSelection(0)
                        }
                        selectedDeviceType = DeviceType.Relay_32.value
                        protocol.isEnabled = true
                    }
                    5 -> {
                        if (selectedDeviceType == DeviceType.CAMERA.value) {
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocol)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                            protocol.setSelection(0)
                        }
                        selectedDeviceType = DeviceType.Dimmer_8.value
                        protocol.isEnabled = true
                    }
                    6 -> {
                        if (selectedDeviceType == DeviceType.CAMERA.value) {
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocol)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                        }
                        selectedDeviceType = DeviceType.COLB.value
                        protocol.setSelection(1)
                        protocol.isEnabled = false
                    }
                    7 -> {
                        if (selectedProtocolType != DeviceType.CAMERA.value) {
                            selectedDeviceType = DeviceType.CAMERA.value
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocolCamera)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                            protocol.setSelection(0)
                            protocol.isEnabled = false
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        selectedProtocolType = ProtocolType.TCP.value
        showTCPInput()

        protocol.onItemSelectedListener = onItemSelectedListener

        back.setOnClickListener {
            onBackPressed()
        }

        history.setOnClickListener {
            val intent = Intent(this@AddDeviceActivity, AddHistoryActivity::class.java)
            startActivityForResult(intent, 2001)
        }

        scan.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        this@AddDeviceActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(this@AddDeviceActivity, ScanActivity::class.java)
                    startActivityForResult(intent, 2000)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 1000)
                }
            } else {
                val intent = Intent(this@AddDeviceActivity, ScanActivity::class.java)
                startActivityForResult(intent, 2000)
            }
        }

        add.setOnClickListener {
            if (selectedProtocolType == ProtocolType.TCP.value) {
                if (ip.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.ip_alert))
                    return@setOnClickListener
                }
                if (port.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.port_alert))
                    return@setOnClickListener
                }
            } else if (selectedProtocolType == ProtocolType.MQTT.value) {
                if (ip.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.ip_alert))
                    return@setOnClickListener
                }
                if (port.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.port_alert))
                    return@setOnClickListener
                }
                if (deviceId.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.device_id_input))
                    return@setOnClickListener
                }
                if (userName.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.user_name_input))
                    return@setOnClickListener
                }
                if (password.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.password_input))
                    return@setOnClickListener
                }
            } else if (selectedProtocolType == ProtocolType.CAMERA.value) {
                if (deviceId.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.device_id_input))
                    return@setOnClickListener
                }
                if (deviceUser.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.user_name_input))
                    return@setOnClickListener
                }
                if (devicePassword.text.toString().isEmpty()) {
                    showToast(resources.getString(R.string.password_input))
                    return@setOnClickListener
                }
                ip.setText("")
                port.setText("0")
            }
            val ipAddress = IPAddress(
                ip.text.toString(),
                Integer.decode(port.text.toString()),
                selectedDeviceType,
                selectedProtocolType,
                userName.text.toString(),
                password.text.toString(),
                deviceId.text.toString(),
                deviceUser.text.toString(),
                devicePassword.text.toString()
            )
            val result = Intent()
            result.putExtra("device_result", Tools.gson.toJson(ipAddress))
            setResult(RESULT_OK, result)

            finish()
        }
    }

    private fun showTCPInput() {
        llIP.visibility = View.VISIBLE
        llPort.visibility = View.VISIBLE
        llProtocol.visibility = View.VISIBLE
        llDeviceId.visibility = View.GONE
        llDeviceUser.visibility = View.GONE
        llDevicePassword.visibility = View.GONE
        llUserName.visibility = View.GONE
        llPassword.visibility = View.GONE
        deviceId.setText("")
        deviceUser.setText("")
        devicePassword.setText("")
        userName.setText("")
        password.setText("")
    }

    private fun showMQTTInput() {
        llIP.visibility = View.VISIBLE
        llPort.visibility = View.VISIBLE
        llProtocol.visibility = View.VISIBLE
        llDeviceId.visibility = View.VISIBLE
        llDeviceUser.visibility = View.GONE
        llDevicePassword.visibility = View.VISIBLE
        llUserName.visibility = View.VISIBLE
        llPassword.visibility = View.VISIBLE
        deviceUser.setText("")
    }

    private fun showCAMERAInput() {
        llIP.visibility = View.GONE
        llPort.visibility = View.GONE
        llProtocol.visibility = View.VISIBLE
        llDeviceId.visibility = View.VISIBLE
        llDeviceUser.visibility = View.VISIBLE
        llDevicePassword.visibility = View.VISIBLE
        llUserName.visibility = View.GONE
        llPassword.visibility = View.GONE
        ip.setText("")
        port.setText("")
        userName.setText("")
        password.setText("")
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2000 && resultCode == Activity.RESULT_OK) {
            val scanResult = data?.getStringExtra("scan_result")
            if (TextUtils.isEmpty(scanResult)) return
            deviceId.setText(scanResult)
        } else
            if (requestCode == 2001 && resultCode == Activity.RESULT_OK) {
                val historyResult = data?.getStringExtra("history_result")
                if (TextUtils.isEmpty(historyResult)) return
                val json = JSONObject(historyResult!!)
                ip.setText(json.optString("ip"))
                port.setText(json.optString("port"))
                selectedProtocolType = json.optInt("protocolType")
                if (selectedProtocolType != ProtocolType.CAMERA.value) {
                    val protocolArray = resources.getStringArray(R.array.protocol)
                    for ((index, item) in protocolArray.withIndex()) {
                        if (item == Tools.getProtocolTypeEnum(selectedProtocolType).protocolTypeName) {
                            val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                                this@AddDeviceActivity,
                                android.R.layout.simple_spinner_item,
                                resources.getTextArray(R.array.protocol)
                            )
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                            protocol.adapter = adapter
                            protocol.onItemSelectedListener = onItemSelectedListener
                            protocol.isEnabled = false
                            protocol.setSelection(index)
                        }
                    }
                }

                selectedDeviceType = json.optInt("deviceType")
                val modelArray = resources.getStringArray(R.array.channel)
                for ((index, item) in modelArray.withIndex()) {
                    if (item == Tools.getDeviceTypeEnum(selectedDeviceType).typeName
                        || item == Tools.getDeviceTypeEnum(selectedDeviceType).typeNameCN
                    ) {
                        model.setSelection(index)
                        break
                    }
                }

                deviceUser.setText(json.optString("deviceUserName"))
                devicePassword.setText(json.optString("devicePassword"))
                userName.setText(json.optString("userName"))
                password.setText(json.optString("password"))
            }
        super.onActivityResult(requestCode, resultCode, data)
    }
}