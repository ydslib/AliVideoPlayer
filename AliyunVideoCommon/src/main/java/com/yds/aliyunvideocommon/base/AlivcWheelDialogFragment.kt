package com.yds.aliyunvideocommon.base

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.yds.aliyunvideocommon.R
import com.yds.aliyunvideocommon.widget.WheelView

class AlivcWheelDialogFragment : BaseDialogFragment(), WheelView.OnValueChangeListener {

    private var mWheelView: WheelView? = null
    private var mTvLeft: TextView? = null
    private var mTvRight: TextView? = null
    var mFragmentManager: FragmentManager? = null

    /**
     * 数据源
     */
    private var mDialogWheel: Array<String>? = null

    /**
     * 左边文字
     */
    private var mDialogLeft: String = ""

    /**
     * 右边文字
     */
    private var mDialogRight: String = ""

    /**
     * 数据接口回调
     */
    private var mOnWheelDialogListener: OnWheelDialogListener? = null

    /**
     * 点击外部是否可以取消
     */
    var mIsCancelableOutside = true

    /**
     * 弹窗动画
     */
    var mDialogAnimationRes = 0

    override fun getLayoutRes() = R.layout.alivc_common_dialogfragment_wheelview

    override fun bindView(view: View?) {
        mTvLeft = view?.findViewById(R.id.alivc_tv_cancel)
        mTvRight = view?.findViewById(R.id.alivc_tv_sure)
        mWheelView = view?.findViewById(R.id.alivc_wheelView_dialog)

        mTvLeft?.text = mDialogLeft
        mTvRight?.text = mDialogRight

        mWheelView?.refreshByNewDisplayedValues(mDialogWheel)
        //设置是否可以上下无限滑动

        //设置是否可以上下无限滑动
        mWheelView?.setWrapSelectorWheel(false)
        mWheelView?.setDividerColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.alivc_common_bg_white_gray
            )
        )
        mWheelView!!.setSelectedTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.alivc_common_bg_black
            )
        )
        mWheelView!!.setNormalTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.alivc_common_font_gray_333333
            )
        )
        initEvent()

    }

    fun initEvent() {
        //左边按钮
        mTvLeft!!.setOnClickListener {
            if (mOnWheelDialogListener != null) {
                mOnWheelDialogListener!!.onClickLeft(this@AlivcWheelDialogFragment, getWheelValue())
            }
        }

        //右边按钮
        mTvRight!!.setOnClickListener {
            if (mOnWheelDialogListener != null) {
                mOnWheelDialogListener!!.onClickRight(
                    this@AlivcWheelDialogFragment,
                    getWheelValue()
                )
            }
        }
    }

    /**
     * 获取当前值
     *
     * @return
     */
    private fun getWheelValue(): String? {
        var result = ""
        mWheelView?.run {
            val content = getDisplayedValues()
            result = content?.get(getValue() - getMinValue()) ?: ""
        }
        return result
    }


    interface OnWheelDialogListener {
        /**
         * 左边按钮单击事件回调
         *
         * @param dialog
         * @param value
         */
        fun onClickLeft(dialog: DialogFragment?, value: String?)

        /**
         * 右边按钮单击事件回调
         *
         * @param dialog
         * @param value
         */
        fun onClickRight(dialog: DialogFragment?, value: String?)

        /**
         * 滑动停止时的回调
         *
         * @param dialog
         * @param value
         */
        fun onValueChanged(dialog: DialogFragment?, value: String?)
    }

    override fun onValueChange(picker: WheelView?, oldVal: Int, newVal: Int) {
        val content = mWheelView?.getDisplayedValues()
        if (content != null && mOnWheelDialogListener != null) {
            mOnWheelDialogListener?.onValueChanged(this,content[newVal - (mWheelView?.getMinValue()?:0)])
        }
    }

    override fun isCancelableOutside() = mIsCancelableOutside
    override fun getDialogAnimationRes() = mDialogAnimationRes

    fun show():AlivcWheelDialogFragment = apply {
        try {
            val ft = mFragmentManager?.beginTransaction()
            ft?.let {
                it.remove(this)
                it.add(this,TAG)
                it.commitAllowingStateLoss()
            }
        }catch (e:Exception){
            Log.e("Dialog", e.toString())
        }
    }

    class Builder(private val fragmentManager: FragmentManager){
        private val mDialogFragment = AlivcWheelDialogFragment()

        init {
            mDialogFragment.mFragmentManager = fragmentManager
        }

        fun setWheelData(data:Array<String>) = apply {
            mDialogFragment.mDialogWheel = data
        }

        fun cancelString(cancel:String) = apply {
            mDialogFragment.mDialogLeft = cancel
        }

        fun sureString(sure:String) = apply {
            mDialogFragment.mDialogRight = sure
        }

        fun onWheelDialogListener(onWheelDialogListener:OnWheelDialogListener) = apply {
            mDialogFragment.mOnWheelDialogListener = onWheelDialogListener
        }

        fun isCancelableOutside(outSide:Boolean) = apply{
            mDialogFragment.mIsCancelableOutside = outSide
        }

        fun dialogAnimationRes(animation:Int) = apply{
            mDialogFragment.mDialogAnimationRes = animation
        }

        fun create():AlivcWheelDialogFragment{
            return mDialogFragment
        }
    }
}