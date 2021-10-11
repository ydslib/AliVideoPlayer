package com.yds.aliyunplayer.widget

import android.view.Surface
import android.view.View

interface IRenderView {
    fun addRenderCallback(renderCallback: IRenderCallback?)

    fun getView(): View?

    interface IRenderCallback {
        fun onSurfaceCreate(surface: Surface?)
        fun onSurfaceChanged(width: Int, height: Int)
        fun onSurfaceDestroyed()
    }
}