package com.yds.aliyunplayer.widget

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.view.Surface
import android.widget.FrameLayout
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.bean.InfoBean
import com.aliyun.player.nativeclass.CacheConfig
import com.aliyun.player.nativeclass.MediaInfo
import com.aliyun.player.nativeclass.PlayerConfig
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.player.source.*
import java.lang.ref.WeakReference

class AliyunRenderView : FrameLayout {
    private var mContext: Context? = null

    /**
     * 真正的播放器实例对象
     */
    private var mAliPlayer: AliPlayer? = null

    /**
     * Surface
     */
    private var mIRenderView: IRenderView? = null
    /**
     * 获取当前解码状态
     * @return true:硬解,false:软解
     */
    /**
     * 判断当前解码状态,true:硬解,false:软解
     * 默认是硬解
     */
    var isHardwareDecoder = true
        private set
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
        mContext = context
        initPlayer()
    }

    private fun initPlayer() {
        mAliPlayer = AliPlayerFactory.createAliPlayer(mContext!!.applicationContext)
        //        mAliPlayer = AliPlayerManager.getPlayerList(mContext.getApplicationContext());
        initPlayerListener()
    }

    private fun initPlayerListener() {
        mAliPlayer?.setOnInfoListener(OnAVPInfoListener(this))
        mAliPlayer?.setOnErrorListener(OnAVPErrorListener(this))
        mAliPlayer?.setOnSeiDataListener(OnAVPSeiDataListener(this))
        mAliPlayer?.setOnSnapShotListener(OnAVPSnapShotListener(this))
        mAliPlayer?.setOnPreparedListener(OnAVPPreparedListener(this))
        mAliPlayer?.setOnCompletionListener(OnAVPCompletionListener(this))
        mAliPlayer?.setOnTrackChangedListener(OnAVPTrackChangedListener(this))
        mAliPlayer?.setOnSeekCompleteListener(OnAVPSeekCompleteListener(this))
        mAliPlayer?.setOnVideoRenderedListener(OnAVPVideoRenderedListener(this))
        mAliPlayer?.setOnLoadingStatusListener(OnAVPLoadingStatusListener(this))
        mAliPlayer?.setOnRenderingStartListener(OnAVPRenderingStartListener(this))
        mAliPlayer?.setOnVerifyTimeExpireCallback(OnAVPVerifyStsCallback(this))
        mAliPlayer?.setOnStateChangedListener(OnAVPStateChangedListener(this))
        mAliPlayer?.setOnSubtitleDisplayListener(OnAVPSubtitleDisplayListener(this))
        mAliPlayer?.setOnVideoSizeChangedListener(OnAVPVideoSizeChangedListener(this))
    }

    enum class SurfaceType {
        /**
         * TextureView
         */
        TEXTURE_VIEW,

        /**
         * SurfacView
         */
        SURFACE_VIEW
    }

    /**
     * 获取真正的播放器实例对象
     */
    val aliPlayer: AliPlayer?
        get() = mAliPlayer

    /**
     * 该方法需要在创建播放器完成后,prepare前调用
     * @param surfaceType  Surface的类型
     */
    fun setSurfaceType(surfaceType: SurfaceType) {
        mIRenderView = if (surfaceType == SurfaceType.TEXTURE_VIEW && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TextureRenderView(mContext!!)
        } else {
            SurfaceRenderView(mContext!!)
        }
        initListener()
        addView(mIRenderView?.getView())
    }

    /**
     * 设置播放源
     */
    fun setDataSource(vidSts: VidSts?) {
        mAliPlayer?.setDataSource(vidSts)
    }

    /**
     * 设置播放源
     */
    fun setDataSource(vidAuth: VidAuth?) {
        mAliPlayer?.setDataSource(vidAuth)
    }

    /**
     * 设置播放源
     */
    fun setDataSource(liveSts: LiveSts?) {
        mAliPlayer?.setDataSource(liveSts)
    }

    /**
     * 设置播放源
     */
    fun setDataSource(vidMps: VidMps?) {
        mAliPlayer?.setDataSource(vidMps)
    }

    /**
     * 设置播放源
     */
    fun setDataSource(urlSource: UrlSource?) {
        mAliPlayer?.setDataSource(urlSource)
    }

    /**
     * 刷新sts信息
     */
    fun updateStsInfo(stsInfo: StsInfo?) {
        mAliPlayer?.updateStsInfo(stsInfo)
    }

    /**
     * 刷新Auth信息
     */
    fun updateAuthInfo(vidAuth: VidAuth?) {
        mAliPlayer?.updateVidAuth(vidAuth)
    }

    /**
     * 设置是否静音
     */
    fun setMute(isMute: Boolean) {
        mAliPlayer?.isMute = isMute
    }
    /**
     * 获取音量
     */
    /**
     * 设置音量
     */
    var volume: Float
        get() = mAliPlayer?.volume ?: 0f
        set(v) {
            mAliPlayer?.volume = v
        }

    /**
     * 是否开启自动播放
     */
    fun setAutoPlay(isAutoPlay: Boolean) {
        mAliPlayer?.setAutoPlay(isAutoPlay)
    }

    /**
     * 设置播放速率
     */
    fun setSpeed(speed: Float) {
        mAliPlayer?.speed = speed
    }

    /**
     * 是否循环播放
     */
    var isLoop: Boolean
        get() {
            return mAliPlayer?.isLoop ?:false
        }
        set(loop) {
            mAliPlayer?.isLoop = loop
        }

    /**
     * 截屏
     */
    fun snapshot() {
        mAliPlayer?.snapshot()
    }

    /**
     * 选择 track
     * @param index 索引
     */
    fun selectTrack(index: Int) {
        mAliPlayer?.selectTrack(index)
    }

    /**
     * 选择 track
     * @param index  索引
     * @param focus  是否强制选择track
     */
    fun selectTrack(index: Int, focus: Boolean) {
        mAliPlayer?.selectTrack(index, focus)
    }

    /**
     * 停止播放
     */
    fun stop() {
        mAliPlayer?.stop()
    }

    /**
     * prepare
     */
    fun prepare() {
        mAliPlayer?.prepare()
    }

    /**
     * 暂停播放,直播流不建议使用
     */
    fun pause() {
        mAliPlayer?.pause()
    }

    fun start() {
        mAliPlayer?.start()
    }

    fun reload() {
        mAliPlayer?.reload()
    }

    /**
     * 获取视频时长
     */
    val duration: Long
        get() {
            return mAliPlayer?.duration ?:0
        }

    /**
     * 获取当前 track
     */
    fun currentTrack(typeVideo: TrackInfo.Type?): TrackInfo? {
        return mAliPlayer?.currentTrack(typeVideo)
    }

    /**
     * 获取当前 track
     */
    @Deprecated("")
    fun currentTrack(ordinal: Int): TrackInfo? {
        return mAliPlayer?.currentTrack(ordinal)
    }

    /**
     * seek
     * @param position  目标位置
     * @param seekMode  精准/非精准seek
     */
    fun seekTo(position: Long, seekMode: IPlayer.SeekMode?) {
        mAliPlayer?.seekTo(position, seekMode)
    }

    private fun initListener() {
        mIRenderView?.addRenderCallback(MyRenderViewCallback(this))
    }

    /**
     * 缓存配置
     */
    fun setCacheConfig(cacheConfig: CacheConfig?) {
        mAliPlayer?.setCacheConfig(cacheConfig)
    }
    /**
     * 获取PlayerConfig
     */
    /**
     * 设置PlayerConfig
     */
    var playerConfig: PlayerConfig?
        get() {
            return mAliPlayer?.config
        }
        set(playerConfig) {
            mAliPlayer?.config = playerConfig
        }
    /**
     * 获取当前缩放模式
     */
    /**
     * 设置缩放模式
     */
    var scaleModel: IPlayer.ScaleMode
        get() {
            return mAliPlayer?.getScaleMode()?:IPlayer.ScaleMode.SCALE_ASPECT_FIT
        }
        set(scaleMode) {
            mAliPlayer?.scaleMode = scaleMode
        }
    /**
     * 获取当前旋转模式
     */
    /**
     * 设置旋转模式
     */
    var rotateModel: IPlayer.RotateMode
        get() {
            return mAliPlayer?.rotateMode ?:IPlayer.RotateMode.ROTATE_0
        }
        set(rotateModel) {
            mAliPlayer?.rotateMode = rotateModel
        }
    /**
     * 获取当前镜像模式
     */
    /**
     * 设置镜像模式
     */
    var mirrorMode: IPlayer.MirrorMode
        get() {
            return mAliPlayer?.mirrorMode ?:IPlayer.MirrorMode.MIRROR_MODE_NONE
        }
        set(mirrorMode) {
            mAliPlayer?.mirrorMode = mirrorMode
        }
    val mediaInfo: MediaInfo?
        get() {
            return mAliPlayer?.mediaInfo
        }

    /**
     * 软硬解开关
     * @param enableHardwareDecoder     true:硬解,false:软解
     */
    fun enableHardwareDecoder(enableHardwareDecoder: Boolean) {
        isHardwareDecoder = enableHardwareDecoder
        mAliPlayer?.enableHardwareDecoder(enableHardwareDecoder)
    }

    fun release() {
        stop()
        mAliPlayer?.setSurface(null)
        mAliPlayer?.release()
        mAliPlayer = null
        mSurface = null
    }

    private class MyRenderViewCallback constructor(aliyunRenderView: AliyunRenderView) :
        IRenderView.IRenderCallback {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onSurfaceCreate(surface: Surface?) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.mSurface = surface
            aliyunRenderView?.mAliPlayer?.setSurface(surface)
        }

        override fun onSurfaceChanged(width: Int, height: Int) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.mAliPlayer?.surfaceChanged()
        }

        override fun onSurfaceDestroyed() {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.mAliPlayer?.setSurface(null)
        }

    }

    /**
     * OnPrepared
     */
    private class OnAVPPreparedListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnPreparedListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onPrepared() {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onPrepared()
        }

    }

    /**
     * 纯音频、纯视频流监听
     */
    interface OnVideoStreamTrackTypeListener {
        //纯视频
        fun onVideoOnlyType()

        //纯音频
        fun onAudioOnlyType()
    }

    private var mOnVideoStreamTrackTypeListener: OnVideoStreamTrackTypeListener? = null
    fun setOnVideoStreamTrackType(listener: OnVideoStreamTrackTypeListener?) {
        mOnVideoStreamTrackTypeListener = listener
    }

    private var mOnPreparedListener: IPlayer.OnPreparedListener? = null
    fun setOnPreparedListener(listener: IPlayer.OnPreparedListener?) {
        mOnPreparedListener = listener
    }

    private fun onPrepared() {
        mOnPreparedListener?.onPrepared()

        mOnVideoStreamTrackTypeListener?.let {
            val trackVideo: TrackInfo? = mAliPlayer?.currentTrack(TrackInfo.Type.TYPE_VIDEO)
            val trackAudio: TrackInfo? = mAliPlayer?.currentTrack(TrackInfo.Type.TYPE_AUDIO)
            if (trackVideo == null && trackAudio != null) {
                it.onAudioOnlyType()
            } else if (trackVideo != null && trackAudio == null) {
                it.onVideoOnlyType()
            }
        }
    }

    /**
     * OnVideoRenderedListener
     */
    private class OnAVPVideoRenderedListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnVideoRenderedListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onVideoRendered(timeMs: Long, pts: Long) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onVideoRendered(timeMs, pts)
        }

    }

    private var mOnVideoRenderedListener: IPlayer.OnVideoRenderedListener? = null
    fun setOnVideoRenderedListener(listener: IPlayer.OnVideoRenderedListener?) {
        mOnVideoRenderedListener = listener
    }

    private fun onVideoRendered(timeMs: Long, pts: Long) {
        mOnVideoRenderedListener?.onVideoRendered(timeMs, pts)
    }

    /**
     * OnRenderingStartListener
     */
    private class OnAVPRenderingStartListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnRenderingStartListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onRenderingStart() {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onRenderingStart()
        }

    }

    private var mOnRenderingStartListener: IPlayer.OnRenderingStartListener? = null
    fun setOnRenderingStartListener(listener: IPlayer.OnRenderingStartListener?) {
        mOnRenderingStartListener = listener
    }

    private fun onRenderingStart() {
        mOnRenderingStartListener?.onRenderingStart()
    }

    /**
     * OnStateChangedListner
     */
    private class OnAVPStateChangedListener(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnStateChangedListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onStateChanged(i: Int) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onStateChangedListener(i)
        }

    }

    private var mOnStateChangedListener: IPlayer.OnStateChangedListener? = null
    fun setOnStateChangedListener(listener: IPlayer.OnStateChangedListener?) {
        mOnStateChangedListener = listener
    }

    private fun onStateChangedListener(newState: Int) {
        mOnStateChangedListener?.onStateChanged(newState)
    }

    /**
     * OnVideoSizeChangedListener
     */
    private class OnAVPVideoSizeChangedListener(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnVideoSizeChangedListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onVideoSizeChanged(width: Int, height: Int) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onVideoSizeChanged(width, height)
        }

    }

    private var mOnVideoSizeChangedListener: IPlayer.OnVideoSizeChangedListener? = null
    fun setOnVideoSizeChangedListener(listener: IPlayer.OnVideoSizeChangedListener?) {
        mOnVideoSizeChangedListener = listener
    }

    private fun onVideoSizeChanged(width: Int, height: Int) {
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener?.onVideoSizeChanged(width, height)
        }
    }

    /**
     * OnInfoListener
     */
    private class OnAVPInfoListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnInfoListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onInfo(infoBean: InfoBean) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onInfo(infoBean)
        }

    }

    private var mOnInfoListener: IPlayer.OnInfoListener? = null
    fun setOnInfoListener(listener: IPlayer.OnInfoListener?) {
        mOnInfoListener = listener
    }

    private fun onInfo(infoBean: InfoBean) {
        mOnInfoListener?.onInfo(infoBean)
    }

    /**
     * OnLoadingStatusListener
     */
    private class OnAVPLoadingStatusListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnLoadingStatusListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onLoadingBegin() {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onLoadingBegin()
        }

        override fun onLoadingProgress(percent: Int, netSpeed: Float) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onLoadingProgress(percent, netSpeed)
        }

        override fun onLoadingEnd() {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onLoadingEnd()
        }

    }

    private var mOnLoadingStatusListener: IPlayer.OnLoadingStatusListener? = null
    fun setOnLoadingStatusListener(listener: IPlayer.OnLoadingStatusListener?) {
        mOnLoadingStatusListener = listener
    }

    private fun onLoadingBegin() {
        mOnLoadingStatusListener?.onLoadingBegin()
    }

    private fun onLoadingProgress(percent: Int, netSpeed: Float) {
        mOnLoadingStatusListener?.onLoadingProgress(percent, netSpeed)
    }

    private fun onLoadingEnd() {
        mOnLoadingStatusListener?.onLoadingEnd()
    }

    /**
     * OnSnapShotListener
     */
    private class OnAVPSnapShotListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnSnapShotListener {
        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onSnapShot(bitmap: Bitmap, with: Int, height: Int) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onSnapShot(bitmap, with, height)
        }

    }

    private var mOnSnapShotListener: IPlayer.OnSnapShotListener? = null
    fun setOnSnapShotListener(listener: IPlayer.OnSnapShotListener?) {
        mOnSnapShotListener = listener
    }

    private fun onSnapShot(bitmap: Bitmap, with: Int, height: Int) {
        mOnSnapShotListener?.onSnapShot(bitmap, with, height)
    }

    /**
     * OnCompletionListener
     */
    private class OnAVPCompletionListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnCompletionListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onCompletion() {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onCompletion()
        }

    }

    private var mOnCompletionListener: IPlayer.OnCompletionListener? = null
    fun setOnCompletionListener(listener: IPlayer.OnCompletionListener?) {
        mOnCompletionListener = listener
    }

    private fun onCompletion() {
        mOnCompletionListener?.onCompletion()
    }

    /**
     * OnSeekCompleteListener
     */
    private class OnAVPSeekCompleteListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnSeekCompleteListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onSeekComplete() {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onSeekComplete()
        }

    }

    private var mOnSeekCompleteListener: IPlayer.OnSeekCompleteListener? = null
    fun setOnSeekCompleteListener(listener: IPlayer.OnSeekCompleteListener?) {
        mOnSeekCompleteListener = listener
    }

    private fun onSeekComplete() {
        mOnSeekCompleteListener?.onSeekComplete()
    }

    /**
     * OnTrackChangedListener
     */
    private class OnAVPTrackChangedListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnTrackChangedListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onChangedSuccess(trackInfo: TrackInfo) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onChangedSuccess(trackInfo)
        }

        override fun onChangedFail(trackInfo: TrackInfo, errorInfo: ErrorInfo) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onChangedFail(trackInfo, errorInfo)
        }

    }

    private var mOnTrackChangedListener: IPlayer.OnTrackChangedListener? = null
    fun setOnTrackChangedListener(listener: IPlayer.OnTrackChangedListener?) {
        mOnTrackChangedListener = listener
    }

    private fun onChangedSuccess(trackInfo: TrackInfo) {
        mOnTrackChangedListener?.onChangedSuccess(trackInfo)
    }

    private fun onChangedFail(trackInfo: TrackInfo, errorInfo: ErrorInfo) {
        mOnTrackChangedListener?.onChangedFail(trackInfo, errorInfo)
    }

    /**
     * OnErrorListener
     */
    private class OnAVPErrorListener constructor(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnErrorListener {
        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onError(errorInfo: ErrorInfo) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onError(errorInfo)
        }

    }

    private var mOnErrorListener: IPlayer.OnErrorListener? = null
    fun setOnErrorListener(listener: IPlayer.OnErrorListener?) {
        mOnErrorListener = listener
    }

    private fun onError(errorInfo: ErrorInfo) {
        mOnErrorListener?.onError(errorInfo)
    }

    /**
     * onSubtitleDisplayListener
     */
    private class OnAVPSubtitleDisplayListener(aliyunRenderView: AliyunRenderView):IPlayer.OnSubtitleDisplayListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onSubtitleExtAdded(trackIndex: Int, url: String) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onSubtitleExtAdded(trackIndex, url)
        }

        override fun onSubtitleShow(trackIndex: Int, id: Long, data: String) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onSubtitleShow(trackIndex, id, data)
        }

        override fun onSubtitleHide(trackIndex: Int, id: Long) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onSubtitleHide(trackIndex, id)
        }

        override fun onSubtitleHeader(p0: Int, p1: String?) {

        }

    }

    private fun onSubtitleHide(trackIndex: Int, id: Long) {
        mOnSubtitleDisplayListener?.onSubtitleHide(trackIndex, id)
    }

    private fun onSubtitleShow(trackIndex: Int, id: Long, data: String) {
        mOnSubtitleDisplayListener?.onSubtitleShow(trackIndex, id, data)
    }

    private fun onSubtitleExtAdded(trackIndex: Int, url: String) {
        mOnSubtitleDisplayListener?.onSubtitleExtAdded(trackIndex, url)
    }

    private var mOnSubtitleDisplayListener: IPlayer.OnSubtitleDisplayListener? = null
    fun setOnSubtitleDisplayListener(listener: IPlayer.OnSubtitleDisplayListener?) {
        mOnSubtitleDisplayListener = listener
    }

    /**
     * onSeiDataListener
     */
    private class OnAVPSeiDataListener(aliyunRenderView: AliyunRenderView) :
        IPlayer.OnSeiDataListener {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onSeiData(type: Int, bytes: ByteArray) {
            val aliyunRenderView = weakReference.get()
            aliyunRenderView?.onSeiData(type, bytes)
        }

    }

    private fun onSeiData(type: Int, bytes: ByteArray) {
        mOnSeiDataListener?.onSeiData(type, bytes)
    }

    private var mOnSeiDataListener: IPlayer.OnSeiDataListener? = null
    fun setOnSeiDataListener(listener: IPlayer.OnSeiDataListener?) {
        mOnSeiDataListener = listener
    }

    private class OnAVPVerifyStsCallback(aliyunRenderView: AliyunRenderView) :
        AliPlayer.OnVerifyTimeExpireCallback {

        private val weakReference: WeakReference<AliyunRenderView> = WeakReference(aliyunRenderView)

        override fun onVerifySts(stsInfo: StsInfo): AliPlayer.Status {
            val aliyunRenderView = weakReference.get()
            return aliyunRenderView?.onVerifySts(stsInfo) ?: AliPlayer.Status.Valid
        }

        override fun onVerifyAuth(vidAuth: VidAuth): AliPlayer.Status {
            val aliyunRenderView = weakReference.get()
            return aliyunRenderView?.onVerifyAuth(vidAuth) ?: AliPlayer.Status.Valid
        }

    }

    private var mOnVerifyTimeExpireCallback: AliPlayer.OnVerifyTimeExpireCallback? = null
    fun setOnVerifyTimeExpireCallback(listener: AliPlayer.OnVerifyTimeExpireCallback?) {
        mOnVerifyTimeExpireCallback = listener
    }

    private fun onVerifyAuth(vidAuth: VidAuth): AliPlayer.Status {
        return mOnVerifyTimeExpireCallback?.onVerifyAuth(vidAuth)?:AliPlayer.Status.Valid
    }

    private fun onVerifySts(stsInfo: StsInfo): AliPlayer.Status {
        return mOnVerifyTimeExpireCallback?.onVerifySts(stsInfo)?:AliPlayer.Status.Valid
    }
}