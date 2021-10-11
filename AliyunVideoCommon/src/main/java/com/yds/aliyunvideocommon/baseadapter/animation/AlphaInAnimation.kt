package com.yds.aliyunvideocommon.baseadapter.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View

class AlphaInAnimation: BaseAnimation{
    private var mFrom:Float = 0f

    companion object{
        private const val DEFAULT_ALPHA_FROM = 0f
    }
    constructor():this(DEFAULT_ALPHA_FROM)

    constructor(from:Float){
        mFrom = from
    }


    override fun getAnimators(view: View?): Array<Animator>? {
        return arrayOf(ObjectAnimator.ofFloat(view, "alpha", mFrom, 1f))
    }
}