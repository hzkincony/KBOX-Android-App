package com.kincony.KControl.ui.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kincony.KControl.R
import com.kincony.KControl.utils.ToastUtils

open abstract class BaseActivity : AppCompatActivity() {
    var mHandler = Handler()

    var canLoad = true
    var loadingCreate = false
    val loading by lazy {
        loadingCreate = true
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
        AlertDialog.Builder(this, R.style.AppTheme_Transparent)
            .setCancelable(true)
            .setView(view)
            .create()
    }

    open fun showLoading() {
        loading?.show()
    }

    open fun closeLoading() {
        loading?.dismiss()
    }

    fun showToast(msg: String?) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Toast.makeText(this@BaseActivity, msg, Toast.LENGTH_SHORT).show()
        } else {
            mHandler.post {
                Toast.makeText(this@BaseActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (canLoad) {
            if (getLayoutId() != 0) {
                setContentView(getLayoutId())
            }
            initView()
        }
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()

    override fun onDestroy() {
        super.onDestroy()
        if (loadingCreate) {
            if (loading?.isShowing == true) {
                loading?.cancel()
            }
        }
        ToastUtils.destroy(this)
    }
}