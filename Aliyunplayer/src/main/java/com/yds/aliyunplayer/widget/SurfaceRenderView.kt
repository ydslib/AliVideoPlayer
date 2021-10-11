package com.yds.aliyunplayer.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

class SurfaceRenderView : SurfaceView, IRenderView, SurfaceHolder.Callback {
    private var mRenderCallback: IRenderView.IRenderCallback? = null

    constructor(context: Context) : super(context) {
        initData(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initData(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initData(context)
    }

    private fun initData(context: Context) {
        Log.e("AliLivePlayerView", "init: SurfaceRenderView")
        holder.addCallback(this)
    }

    override fun addRenderCallback(renderCallback: IRenderView.IRenderCallback?) {
        mRenderCallback = renderCallback
    }

    override fun getView(): View {
        return this
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        mRenderCallback?.onSurfaceCreate(surfaceHolder.surface)
    }

    override fun surfaceChanged(
        surfaceHolder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        mRenderCallback?.onSurfaceChanged(width, height)
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        val surface = surfaceHolder.surface
        surface?.release()
        mRenderCallback?.onSurfaceDestroyed()
    }
}