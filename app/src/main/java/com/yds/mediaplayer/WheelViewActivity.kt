package com.yds.mediaplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.yds.mediaplayer.databinding.ActivityWheelViewBinding

class WheelViewActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = DataBindingUtil.setContentView<ActivityWheelViewBinding>(this,R.layout.activity_wheel_view)
        mBinding.wheelView.refreshByNewDisplayedValues(arrayOf("one","two","three","four"))
        //设置是否可以上下无限滑动
        mBinding.wheelView.setWrapSelectorWheel(false)
        mBinding.wheelView.setDividerColor(ContextCompat.getColor(this, R.color.alivc_common_bg_white_gray))
        mBinding.wheelView.setSelectedTextColor(
            ContextCompat.getColor(
                this,
                R.color.alivc_common_bg_black
            )
        )
        mBinding.wheelView.setNormalTextColor(
            ContextCompat.getColor(
                this,
                R.color.alivc_common_font_gray_333333
            )
        )
    }
}