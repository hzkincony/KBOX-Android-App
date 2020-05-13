package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.kincony.KControl.R
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.ui.base.FragmentHolder
import com.kincony.KControl.ui.fragment.HomeFragment
import com.kincony.KControl.ui.fragment.SettingFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    var fragmentHome = FragmentHolder()
    var fragmentSetting = FragmentHolder()
    var current: FragmentHolder? = null

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        fragmentHome.view = device
        fragmentHome.mClx = HomeFragment::class.java
        fragmentHome.mTag = HomeFragment::class.java.name

        fragmentSetting.view = setting
        fragmentSetting.mClx = SettingFragment::class.java
        fragmentSetting.mTag = SettingFragment::class.java.name

        device.setOnClickListener {
            onSelectFragment(fragmentHome)
        }
        setting.setOnClickListener {
            onSelectFragment(fragmentSetting)
        }

        onSelectFragment(fragmentHome)

    }

    private fun onSelectFragment(newFragmentHolder: FragmentHolder) {
        var oldFragment = current
        onChangeFragment(oldFragment, newFragmentHolder)
        current = newFragmentHolder
    }

    private fun onChangeFragment(oldHolder: FragmentHolder?, newHolder: FragmentHolder) {
        oldHolder?.view?.isSelected = false
        newHolder?.view?.isSelected = true
        val ft = supportFragmentManager.beginTransaction()
        if (oldHolder != null) {
            if (oldHolder.mFragment != null) {
                ft.hide(oldHolder.mFragment!!)
            }
        }
        if (newHolder.mFragment == null) {
            var temp = supportFragmentManager.findFragmentByTag(newHolder.mTag)
            if (temp != null) {
                newHolder.mFragment = temp
            } else {
                newHolder.mFragment = Fragment.instantiate(this, newHolder.mClx?.name ?: "")
            }
            if (newHolder.mFragment?.isAdded != true) {
                ft.add(R.id.container, newHolder.mFragment!!, newHolder.mTag)
            }
        }
        ft.show(newHolder.mFragment!!)
        ft.commit()
    }


    private var time: Long = 0
    override fun onBackPressed() {
        if (System.currentTimeMillis() - time > 2000) {
            showToast(resources.getString(R.string.again))
            time = System.currentTimeMillis()
        } else {
            super.onBackPressed()
        }
    }
}
