package com.yds.mediaplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.yds.aliyunvideocommon.baseadapter.animation.AlphaInAnimation
import com.yds.mediaplayer.databinding.ActivityAlphaInAnimBinding

class AlphaInAnimActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityAlphaInAnimBinding>(this,R.layout.activity_alpha_in_anim)

        binding.start.setOnClickListener {
            val anim = AlphaInAnimation()
            anim.getAnimators(binding.test)?.forEach {
                it.duration = 10000
                it.start()
            }
        }

    }
}