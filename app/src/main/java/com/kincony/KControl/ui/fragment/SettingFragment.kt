package com.kincony.KControl.ui.fragment

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.kincony.KControl.R
import com.kincony.KControl.ui.AddressActivity
import com.kincony.KControl.ui.SceneActivity
import com.kincony.KControl.ui.base.BaseFragment
import com.kincony.KControl.utils.BatteryUtils
import com.kincony.KControl.utils.ToastUtils
import com.kincony.KControl.utils.Tools
import kotlinx.android.synthetic.main.fragment_setting.*


class SettingFragment : BaseFragment() {
    override fun getLayoutId() = R.layout.fragment_setting

    override fun initView() {
        device.setOnClickListener {
            AddressActivity.start(context)
        }
        ignoring_battery_optimizations.setOnClickListener {
            if (!BatteryUtils.isIgnoringBatteryOptimizations(activity)) {
                BatteryUtils.requestIgnoreBatteryOptimizations(this@SettingFragment, 1000)
            }
        }
//        boot_start_management.setOnClickListener {
//            BatteryUtils.requestBootStart(activity)
//        }
        mode.setOnClickListener {
            SceneActivity.start(context)
        }
        versionName.text = Tools.getAppVersionName(context)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1000 && !BatteryUtils.isIgnoringBatteryOptimizations(activity)) {
            ToastUtils.showToastLong(getString(R.string.ignoring_battery_optimizations_fail))
            AlertDialog.Builder(activity!!)
                .setCancelable(true)
                .setMessage(R.string.ignoring_battery_optimizations_fail)
                .setPositiveButton(resources.getString(R.string.boot_start_management)) { _, _ ->
                    BatteryUtils.requestBootStart(activity!!)
                }
                .create()
                .show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}