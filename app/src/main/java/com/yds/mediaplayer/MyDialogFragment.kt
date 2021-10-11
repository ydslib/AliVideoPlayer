package com.yds.mediaplayer

import android.view.Gravity
import android.view.View
import com.yds.aliyunvideocommon.base.BaseDialogFragment

class MyDialogFragment : BaseDialogFragment() {

    override fun getLayoutRes() = R.layout.dialog_fragment_my

    override fun bindView(view: View?) {

    }

    override fun getGravity(): Int {
        return Gravity.CENTER
    }
}