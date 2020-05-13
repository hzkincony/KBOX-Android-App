package com.kincony.KControl.ui.base

import android.view.View
import androidx.fragment.app.Fragment

class FragmentHolder {
    var mFragment: Fragment? = null
    var mClx: Class<*>? = null
    var mTag: String = ""
    var view: View? = null
}