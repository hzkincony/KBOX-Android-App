package com.kincony.KControl.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.kincony.KControl.R
import com.kincony.KControl.utils.ToastUtils

open abstract class BaseFragment : Fragment() {
    var loadingCreate = false
    val loading by lazy {
        loadingCreate = true
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        if (context != null) {
            AlertDialog.Builder(context!!, R.style.AppTheme_Transparent)
                .setCancelable(true)
                .setView(view)
                .create()
        } else {
            null
        }
    }

    val sp by lazy {
        context?.getSharedPreferences(
            getString(R.string.key_sp),
            Context.MODE_PRIVATE
        )
    }

    open fun getLayoutId(): Int = 0

    open fun initView() {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mRootView = inflater.inflate(this.getLayoutId(), container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initView()
    }

    fun showLoading() {
        loading?.show()
    }

    fun closeLoading() {
        loading?.dismiss()
    }

    @Synchronized
    open fun setValue(key: String, value: String) {
        sp?.edit()?.putString(key, value)?.commit()
    }

    @Synchronized
    open fun getValue(key: String, dfValue: String = ""): String {
        return sp?.getString(key, dfValue) ?: dfValue
    }


    fun showToast(msg: String?) {
        ToastUtils.showToastShort(msg)
    }

}