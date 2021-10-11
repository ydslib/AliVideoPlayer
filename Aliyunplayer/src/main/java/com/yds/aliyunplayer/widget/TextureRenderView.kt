package com.yds.aliyunplayer.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View

class TextureRenderView : TextureView, IRenderView, SurfaceTextureListener {
    private var mRenderCallback: IRenderView.IRenderCallback? = null
    private var mSurface: Surface? = null

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
        Log.e("AliLivePlayerView", "init: TextureRenderView")
        surfaceTextureListener = this
    }

    override fun addRenderCallback(renderCallback: IRenderView.IRenderCallback?) {
        mRenderCallback = renderCallback
    }

    override fun getView(): View {
        return this
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        mSurface = Surface(surfaceTexture)
        mRenderCallback?.onSurfaceCreate(mSurface)
    }

    override fun onSurfaceTextureSizeChanged(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        mRenderCallback?.onSurfaceChanged(width, height)
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        mSurface!!.release()
        mRenderCallback?.onSurfaceDestroyed()
        return false
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
}