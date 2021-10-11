package com.yds.aliyunvideocommon.base

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager


/**
 * 所有自定义对话框的基类
 * 用来实现复杂的弹框样式
 */
abstract class BaseDialogFragment : DialogFragment() {

    val TAG: String = javaClass.simpleName

    //默认透明度
    companion object {
        const val DEFAULT_DIMAMOUNT = 0.2f
        fun getScreenWidth(context: Context): Int {
            return context.resources.displayMetrics.widthPixels
        }

        fun getScreenHeight(context: Context):Int{
            return context.resources.displayMetrics.heightPixels
        }
    }

    //填充视图
    abstract fun getLayoutRes(): Int

    //设置视图内容
    abstract fun bindView(view: View?)

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val layoutParams: WindowManager.LayoutParams = it.attributes
            layoutParams.width =
                if (getDialogWidth() > 0) getDialogWidth() else WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height =
                if (getDialogHeight() > 0) getDialogHeight() else WindowManager.LayoutParams.WRAP_CONTENT
            //透明度
            layoutParams.dimAmount = getDimAmount()
            //位置
            layoutParams.gravity = getGravity()
            it.attributes = layoutParams
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        var view: View? = null
        if (getLayoutRes() > 0) {
            view = inflater.inflate(getLayoutRes(), container, false)
        }
        bindView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = dialog
        dialog?.run {
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(isCancelableOutside())
            if (window != null && getDialogAnimationRes() > 0) {
                window!!.setWindowAnimations(getDialogAnimationRes())
            }
            if (getOnKeyListener() != null) {
                setOnKeyListener(getOnKeyListener())
            }
        }

    }

    open fun getOnKeyListener(): DialogInterface.OnKeyListener? {
        return null
    }

    open fun getDialogWidth(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    open fun getDialogHeight(): Int {
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    open fun getDimAmount(): Float {
        return DEFAULT_DIMAMOUNT
    }

    open fun getGravity(): Int {
        return Gravity.BOTTOM
    }

    open fun isCancelableOutside(): Boolean {
        return true
    }

    open fun getDialogAnimationRes(): Int {
        return 0
    }

    fun getFragmentTag(): String {
        return TAG
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, getFragmentTag())
    }


}