package com.kincony.KControl.ui.fragment

import com.kincony.KControl.R
import com.kincony.KControl.ui.AddressActivity
import com.kincony.KControl.ui.SceneActivity
import com.kincony.KControl.ui.base.BaseFragment
import com.kincony.KControl.utils.Tools
import kotlinx.android.synthetic.main.fragment_setting.*


class SettingFragment : BaseFragment() {
    override fun getLayoutId() = R.layout.fragment_setting

    override fun initView() {
        device.setOnClickListener {
            AddressActivity.start(context)
        }
        mode.setOnClickListener {
            SceneActivity.start(context)
        }
        versionName.text = Tools.getAppVersionName(context)
    }


}