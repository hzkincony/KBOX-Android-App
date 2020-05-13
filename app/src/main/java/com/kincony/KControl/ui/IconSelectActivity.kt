package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import com.kincony.KControl.R
import com.kincony.KControl.net.data.IconEvent
import com.kincony.KControl.ui.adapter.IconAdapter
import com.kincony.KControl.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_icon_select.*
import org.greenrobot.eventbus.EventBus

class IconSelectActivity : BaseActivity() {
    var icons = arrayListOf(
        R.drawable.icon5,
        R.drawable.icon6,
        R.drawable.icon7,
        R.drawable.icon8,
        R.drawable.icon9,
        R.drawable.icon10,
        R.drawable.icon11,
        R.drawable.icon12,
        R.drawable.icon13,
        R.drawable.icon14,
        R.drawable.icon15,
        R.drawable.icon16,
        R.drawable.set1,
        R.drawable.set2,
        R.drawable.set3,
        R.drawable.set4,
        R.drawable.set5,
        R.drawable.set6,
        R.drawable.set7,
        R.drawable.icon_up,
        R.drawable.icon_down,
        R.drawable.icon_left,
        R.drawable.icon_right
    )

    companion object {
        fun start(context: Context?, icon: Int, code: Int = 0) {
            val intent = Intent(context, IconSelectActivity::class.java)
            intent.putExtra("icon", icon)
            intent.putExtra("code", code)
            context?.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_icon_select
    }

    override fun initView() {
        back.setOnClickListener {
            finish()
        }
        var adapter = IconAdapter()
        adapter.setNewInstance(icons)
        recycler.adapter = adapter
        recycler.layoutManager = GridLayoutManager(this, 4)

        var code = intent.getIntExtra("code", 0)
        adapter.setOnItemClickListener { adapter, view, position ->
            var icon = icons[position]
            EventBus.getDefault().post(IconEvent(icon, code))
            finish()
        }
    }


}
