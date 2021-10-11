package com.yds.aliyunvideocommon.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.core.widget.ScrollerCompat
import com.yds.aliyunvideocommon.R
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class WheelView : View {

    companion object {
        // default text color of not selected item
        private const val DEFAULT_TEXT_COLOR_NORMAL: Int = 0XFF333333.toInt()

        // default text color of selected item
        private const val DEFAULT_TEXT_COLOR_SELECTED: Int = 0XFFF56313.toInt()

        // default text size of normal item
        private const val DEFAULT_TEXT_SIZE_NORMAL_SP = 14

        // default text size of selected item
        private const val DEFAULT_TEXT_SIZE_SELECTED_SP = 16

        // default text size of hint text, the middle item's right text
        private const val DEFAULT_TEXT_SIZE_HINT_SP = 14

        // distance between selected text and hint text
        private const val DEFAULT_MARGIN_START_OF_HINT_DP = 8

        // distance between hint text and right of this view, used in wrap_content mode
        private const val DEFAULT_MARGIN_END_OF_HINT_DP = 8

        // default divider's color
        private const val DEFAULT_DIVIDER_COLOR: Int = 0XFFF56313.toInt()

        // default divider's height
        private const val DEFAULT_DIVIDER_HEIGHT = 2

        // default divider's margin to the left & right of this view
        private const val DEFAULT_DIVIDER_MARGIN_HORIZONTAL = 0

        // default shown items' count, now we display 3 items, the 2nd one is selected
        private const val DEFAULT_SHOW_COUNT = 3

        // default items' horizontal padding, left padding and right padding are both 5dp,
        // only used in wrap_content mode
        private const val DEFAULT_ITEM_PADDING_DP_H = 5

        // default items' vertical padding, top padding and bottom padding are both 2dp,
        // only used in wrap_content mode
        private const val DEFAULT_ITEM_PADDING_DP_V = 2

        // message's what argument to refresh current state, used by mHandler
        private const val HANDLER_WHAT_REFRESH = 1

        // message's what argument to respond value changed event, used by mHandler
        private const val HANDLER_WHAT_LISTENER_VALUE_CHANGED = 2

        // interval time to scroll the distance of one item's height
        private const val HANDLER_INTERVAL_REFRESH = 30 //millisecond


        // in millisecond unit, default duration of scrolling an item' distance
        private const val DEFAULT_INTERVAL_REVISE_DURATION = 300

        // max and min durations when scrolling from one value to another
        private const val DEFAULT_MIN_SCROLL_BY_INDEX_DURATION =
            DEFAULT_INTERVAL_REVISE_DURATION * 1
        private const val DEFAULT_MAX_SCROLL_BY_INDEX_DURATION =
            DEFAULT_INTERVAL_REVISE_DURATION * 2
    }


    private var mTextColorNormal = DEFAULT_TEXT_COLOR_NORMAL
    private var mTextColorSelected = DEFAULT_TEXT_COLOR_SELECTED
    private var mTextColorHint = DEFAULT_TEXT_COLOR_SELECTED
    private var mTextSizeNormal = 0
    private var mTextSizeSelected = 0
    private var mTextSizeHint = 0
    private var mWidthOfHintText = 0
    private var mWidthOfAlterHint = 0
    private var mMarginStartOfHint = 0
    private var mMarginEndOfHint = 0
    private var mItemPaddingVertical = 0
    private var mItemPaddingHorizental = 0
    private var mDividerColor = DEFAULT_DIVIDER_COLOR
    private var mDividerHeight = DEFAULT_DIVIDER_HEIGHT
    private var mDividerMarginL = DEFAULT_DIVIDER_MARGIN_HORIZONTAL
    private var mDividerMarginR = DEFAULT_DIVIDER_MARGIN_HORIZONTAL
    private var mShowCount = DEFAULT_SHOW_COUNT
    private var mDividerIndex0 = 0
    private var mDividerIndex1 = 0
    private var mMinShowIndex = -1
    private var mMaxShowIndex = -1
    private var mMinValue = 0 //compat for android.widget.NumberPicker

    private var mMaxValue = 0 //compat for android.widget.NumberPicker

    private var mMaxWidthOfDisplayedValues = 0
    private var mMaxHeightOfDisplayedValues = 0
    private var mMaxWidthOfAlterArrayWithMeasureHint = 0
    private var mMaxWidthOfAlterArrayWithoutMeasureHint = 0
    private var mPrivPickedIndex = 0
    private var mMiniVerlocityFling = 150
    private var mScaledTouchSlop = 8
    private var mHintText: String? = null
    private var mEmptyItemHint: String? = null
    private var mAlterHint: String? = null
    private var mFriction = 1f //friction used by scroller when fling

    private var mTextSizeNormalCenterYOffset = 0f
    private var mTextSizeSelectedCenterYOffset = 0f
    private var mTextSizeHintCenterYOffset = 0f
    private var mShowDivider = true //true to show the two dividers

    private var mWrapSelectorWheel = true //true to wrap the displayed values

    private var mCurrentItemIndexEffect = false
    private var mHasInit = false //true if it has initialized

    // if displayed values' number is less than show count, then this value will be false.
    private var mWrapSelectorWheelCheck = true //mDisplayedValues.length<=showcount时，check=false

    // if you want you set to linear mode from wrap mode when scrolling, then this value will be true.
    private var mPendingWrapToLinear = false

    private var mScroller: ScrollerCompat? = null
    private var mVelocityTracker: VelocityTracker? = null

    private val mPaintDivider = Paint()
    private val mPaintText: Paint = Paint()
    private val mPaintHint: Paint = Paint()

    private var mDisplayedValues: Array<String>? = null
    private var mAlterTextArrayWithMeasureHint: Array<CharSequence>? = null
    private var mAlterTextArrayWithoutMeasureHint: Array<CharSequence>? = null

    private var mHandlerThread: HandlerThread? = null
    private var mHandler: Handler? = null
    private var mHandlerLayout: Handler? = null

    // compatible for NumberPicker
    interface OnValueChangeListener {
        fun onValueChange(picker: WheelView?, oldVal: Int, newVal: Int)
    }

    interface OnValueChangeListenerRelativeToRaw {
        fun onValueChangeRelativeToRaw(
            picker: WheelView?,
            oldPickedIndex: Int,
            newPickedIndex: Int,
            displayedValues: Array<String>?
        )
    }

    // compatible for NumberPicker
    interface OnScrollListener {
        fun onScrollStateChange(view: WheelView?, scrollState: Int)

        companion object {
            val SCROLL_STATE_IDLE = 0
            val SCROLL_STATE_TOUCH_SCROLL = 1
            val SCROLL_STATE_FLING = 2
        }
    }

    private var mOnValueChangeListenerRaw: OnValueChangeListenerRelativeToRaw? = null
    private var mOnValueChangeListener //compatible for NumberPicker
            : OnValueChangeListener? = null
    private var mOnScrollListener //compatible for NumberPicker
            : OnScrollListener? = null

    // The current scroll state of the NumberPickerView.
    private var mScrollState = OnScrollListener.SCROLL_STATE_IDLE

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttr(context, attrs)
        init(context)
    }

    private fun initAttr(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.WheelView)
        val n = a.indexCount
        for (i in 0 until n) {
            when (val attr = a.getIndex(i)) {
                R.styleable.WheelView_npv_ShowCount -> {
                    mShowCount = a.getInt(attr, DEFAULT_SHOW_COUNT)
                }
                R.styleable.WheelView_npv_DividerColor -> {
                    mDividerColor = a.getColor(attr, DEFAULT_DIVIDER_COLOR)
                }
                R.styleable.WheelView_npv_DividerHeight -> {
                    mDividerHeight = a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_HEIGHT)
                }
                R.styleable.WheelView_npv_DividerMarginLeft -> {
                    mDividerMarginL =
                        a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_MARGIN_HORIZONTAL)
                }
                R.styleable.WheelView_npv_DividerMarginRight -> {
                    mDividerMarginR =
                        a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_MARGIN_HORIZONTAL)
                }
                R.styleable.WheelView_npv_TextArray -> {
                    mDisplayedValues = convertCharSequenceArrayToStringArray(a.getTextArray(attr))
                }
                R.styleable.WheelView_npv_TextColorNormal -> {
                    mTextColorNormal = a.getColor(attr, DEFAULT_TEXT_COLOR_NORMAL)
                }
                R.styleable.WheelView_npv_TextColorSelected -> {
                    mTextColorSelected = a.getColor(attr, DEFAULT_TEXT_COLOR_SELECTED)
                }
                R.styleable.WheelView_npv_TextColorHint -> {
                    mTextColorHint = a.getColor(attr, DEFAULT_TEXT_COLOR_SELECTED)
                }
                R.styleable.WheelView_npv_TextSizeNormal -> {
                    mTextSizeNormal = a.getDimensionPixelSize(
                        attr,
                        sp2px(context, DEFAULT_TEXT_SIZE_NORMAL_SP.toFloat())
                    )
                }
                R.styleable.WheelView_npv_TextSizeSelected -> {
                    mTextSizeSelected = a.getDimensionPixelSize(
                        attr,
                        sp2px(context, DEFAULT_TEXT_SIZE_SELECTED_SP.toFloat())
                    )
                }
                R.styleable.WheelView_npv_TextSizeHint -> {
                    mTextSizeHint = a.getDimensionPixelSize(
                        attr,
                        sp2px(context, DEFAULT_TEXT_SIZE_HINT_SP.toFloat())
                    )
                }
                R.styleable.WheelView_npv_MinValue -> {
                    mMinShowIndex = a.getInteger(attr, 0)
                }
                R.styleable.WheelView_npv_MaxValue -> {
                    mMaxShowIndex = a.getInteger(attr, 0)
                }
                R.styleable.WheelView_npv_WrapSelectorWheel -> {
                    mWrapSelectorWheel = a.getBoolean(attr, true)
                }
                R.styleable.WheelView_npv_ShowDivider -> {
                    mShowDivider = a.getBoolean(attr, true)
                }
                R.styleable.WheelView_npv_HintText -> {
                    mHintText = a.getString(attr)
                }
                R.styleable.WheelView_npv_AlternativeHint -> {
                    mAlterHint = a.getString(attr)
                }
                R.styleable.WheelView_npv_EmptyItemHint -> {
                    mEmptyItemHint = a.getString(attr)
                }
                R.styleable.WheelView_npv_MarginStartOfHint -> {
                    mMarginStartOfHint = a.getDimensionPixelSize(
                        attr,
                        dp2px(context, DEFAULT_MARGIN_START_OF_HINT_DP.toFloat())
                    )
                }
                R.styleable.WheelView_npv_MarginEndOfHint -> {
                    mMarginEndOfHint = a.getDimensionPixelSize(
                        attr,
                        dp2px(context, DEFAULT_MARGIN_END_OF_HINT_DP.toFloat())
                    )
                }
                R.styleable.WheelView_npv_ItemPaddingVertical -> {
                    mItemPaddingVertical = a.getDimensionPixelSize(
                        attr,
                        dp2px(context, DEFAULT_ITEM_PADDING_DP_V.toFloat())
                    )
                }
                R.styleable.WheelView_npv_ItemPaddingHorizental -> {
                    mItemPaddingHorizental = a.getDimensionPixelSize(
                        attr,
                        dp2px(context, DEFAULT_ITEM_PADDING_DP_H.toFloat())
                    )
                }
                R.styleable.WheelView_npv_AlternativeTextArrayWithMeasureHint -> {
                    mAlterTextArrayWithMeasureHint = a.getTextArray(attr)
                }
                R.styleable.WheelView_npv_AlternativeTextArrayWithoutMeasureHint -> {
                    mAlterTextArrayWithoutMeasureHint = a.getTextArray(attr)
                }
            }
        }
        a.recycle()
    }

    private fun init(context: Context) {
        mScroller = ScrollerCompat.create(context)
        mMiniVerlocityFling = ViewConfiguration.get(getContext()).scaledMinimumFlingVelocity
        mScaledTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop
        if (mTextSizeNormal == 0) {
            mTextSizeNormal = sp2px(context, DEFAULT_TEXT_SIZE_NORMAL_SP.toFloat())
        }
        if (mTextSizeSelected == 0) {
            mTextSizeSelected = sp2px(context, DEFAULT_TEXT_SIZE_SELECTED_SP.toFloat())
        }
        if (mTextSizeHint == 0) {
            mTextSizeHint = sp2px(context, DEFAULT_TEXT_SIZE_HINT_SP.toFloat())
        }
        if (mMarginStartOfHint == 0) {
            mMarginStartOfHint = dp2px(context, DEFAULT_MARGIN_START_OF_HINT_DP.toFloat())
        }
        if (mMarginEndOfHint == 0) {
            mMarginEndOfHint = dp2px(context, DEFAULT_MARGIN_END_OF_HINT_DP.toFloat())
        }
        mPaintDivider.color = mDividerColor
        mPaintDivider.isAntiAlias = true
        mPaintDivider.style = Paint.Style.STROKE
        mPaintDivider.strokeWidth = mDividerHeight.toFloat()
        mPaintText.color = mTextColorNormal
        mPaintText.isAntiAlias = true
        mPaintText.textAlign = Align.CENTER
        mPaintHint.color = mTextColorHint
        mPaintHint.isAntiAlias = true
        mPaintHint.textAlign = Align.CENTER
        mPaintHint.textSize = mTextSizeHint.toFloat()
        if (mShowCount % 2 == 0) {
            mShowCount++
        }
        if (mMinShowIndex == -1 || mMaxShowIndex == -1) {
            updateValueForInit()
        }
        initHandler()
    }

    private fun initHandler() {
        mHandlerThread = HandlerThread("HandlerThread-For-Refreshing")
        mHandlerThread!!.start()
        mHandler = object : Handler(mHandlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    HANDLER_WHAT_REFRESH -> if (!mScroller!!.isFinished) {
                        if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                        mHandler!!.sendMessageDelayed(
                            getMsg(HANDLER_WHAT_REFRESH, 0, 0, msg.obj)!!,
                            HANDLER_INTERVAL_REFRESH.toLong()
                        )
                    } else {
                        var duration = 0
                        val willPickIndex: Int
                        //if scroller finished(not scrolling), then adjust the position
                        if (mCurrDrawFirstItemY != 0) { //need to adjust
                            if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                                onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                            }
                            if (mCurrDrawFirstItemY < -mItemHeight / 2) {
                                //adjust to scroll upward
                                duration =
                                    (DEFAULT_INTERVAL_REVISE_DURATION.toFloat() * (mItemHeight + mCurrDrawFirstItemY) / mItemHeight).toInt()
                                mScroller!!.startScroll(
                                    0,
                                    mCurrDrawGlobalY,
                                    0,
                                    mItemHeight + mCurrDrawFirstItemY,
                                    duration * 2
                                )
                                willPickIndex =
                                    getWillPickIndexByGlobalY(mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY)
                            } else {
                                //adjust to scroll downward
                                duration =
                                    (DEFAULT_INTERVAL_REVISE_DURATION.toFloat() * -mCurrDrawFirstItemY / mItemHeight).toInt()
                                mScroller!!.startScroll(
                                    0,
                                    mCurrDrawGlobalY,
                                    0,
                                    mCurrDrawFirstItemY,
                                    duration * 2
                                )
                                willPickIndex =
                                    getWillPickIndexByGlobalY(mCurrDrawGlobalY + mCurrDrawFirstItemY)
                            }
                            postInvalidate()
                        } else {
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                            //get the index which will be selected
                            willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY)
                        }
                        mHandler!!.sendMessageDelayed(
                            getMsg(
                                HANDLER_WHAT_LISTENER_VALUE_CHANGED,
                                mPrivPickedIndex,
                                willPickIndex,
                                msg.obj
                            )!!, (duration * 2).toLong()
                        )
                    }
                    HANDLER_WHAT_LISTENER_VALUE_CHANGED -> {
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                        if (msg.arg1 != msg.arg2) {
                            if (msg.obj == null || msg.obj !is Boolean || msg.obj as Boolean) {
                                if (mOnValueChangeListener != null) {
                                    mOnValueChangeListener!!.onValueChange(
                                        this@WheelView,
                                        msg.arg1 + mMinValue,
                                        msg.arg2 + mMinValue
                                    )
                                }
                                if (mOnValueChangeListenerRaw != null) {
                                    mOnValueChangeListenerRaw!!.onValueChangeRelativeToRaw(
                                        this@WheelView,
                                        msg.arg1,
                                        msg.arg2,
                                        mDisplayedValues
                                    )
                                }
                            }
                        }
                        mPrivPickedIndex = msg.arg2
                        if (mPendingWrapToLinear) {
                            mPendingWrapToLinear = false
                            internalSetWrapToLinear()
                        }
                    }
                }
            }
        }
        mHandlerLayout = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                requestLayout()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        updateMaxWHOfDisplayedValues(false)
        setMeasuredDimension(
            measureWidth(widthMeasureSpec),
            measureHeight(heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
        mItemHeight = mViewHeight / mShowCount
        mViewCenterX = (mViewWidth + paddingLeft - paddingRight).toFloat() / 2
        var defaultValue = 0
        if (getOneRecycleSize() > 1) {
            if (mHasInit) {
                defaultValue = getValue() - mMinValue
            } else if (mCurrentItemIndexEffect) {
                defaultValue = mCurrDrawFirstItemIndex + (mShowCount - 1) / 2
            } else {
                defaultValue = 0
            }
        }
        correctPositionByDefaultValue(defaultValue, mWrapSelectorWheel && mWrapSelectorWheelCheck)
        updateFontAttr()
        updateNotWrapYLimit()
        updateDividerAttr()
        mHasInit = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandlerThread!!.quit()
    }

    fun getOneRecycleSize(): Int {
        return mMaxShowIndex - mMinShowIndex + 1
    }

    fun getRawContentSize(): Int {
        return if (mDisplayedValues != null) {
            mDisplayedValues!!.size
        } else 0
    }

    fun setDisplayedValuesAndPickedIndex(
        newDisplayedValues: Array<String>?,
        pickedIndex: Int,
        needRefresh: Boolean
    ) {
        stopScrolling()
        if (newDisplayedValues == null) {
            throw IllegalArgumentException("newDisplayedValues should not be null.")
        }
        if (pickedIndex < 0) {
            throw IllegalArgumentException("pickedIndex should not be negative, now pickedIndex is $pickedIndex")
        }
        updateContent(newDisplayedValues)
        updateMaxWHOfDisplayedValues(true)
        updateNotWrapYLimit()
        updateValue()
        mPrivPickedIndex = pickedIndex + mMinShowIndex
        correctPositionByDefaultValue(pickedIndex, mWrapSelectorWheel && mWrapSelectorWheelCheck)
        if (needRefresh) {
            mHandler!!.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH)!!, 0)
            postInvalidate()
        }
    }

    fun setDisplayedValues(newDisplayedValues: Array<String>?, needRefresh: Boolean) {
        setDisplayedValuesAndPickedIndex(newDisplayedValues, 0, needRefresh)
    }

    fun setDisplayedValues(newDisplayedValues: Array<String>?) {
        stopScrolling()
        if (newDisplayedValues == null) {
            throw IllegalArgumentException("newDisplayedValues should not be null.")
        }
        if (mMaxValue - mMinValue + 1 > newDisplayedValues.size) {
            throw IllegalArgumentException(
                "mMaxValue - mMinValue + 1 should not be larger than mDisplayedValues.length, now "
                        + "((mMaxValue - mMinValue + 1) is " + (mMaxValue - mMinValue + 1)
                        + " newDisplayedValues.length is " + newDisplayedValues.size
                        + ", you need to set MaxValue and MinValue before setDisplayedValues(String[])"
            )
        }
        updateContent(newDisplayedValues)
        updateMaxWHOfDisplayedValues(true)
        mPrivPickedIndex = 0 + mMinShowIndex
        correctPositionByDefaultValue(0, mWrapSelectorWheel && mWrapSelectorWheelCheck)
        postInvalidate()
        mHandlerLayout!!.sendEmptyMessage(0)
    }

    /**
     * Gets the values to be displayed instead of string values.
     * @return The displayed values.
     */
    fun getDisplayedValues(): Array<String>? {
        return mDisplayedValues
    }

    fun setWrapSelectorWheel(wrapSelectorWheel: Boolean) {
        if (mWrapSelectorWheel != wrapSelectorWheel) {
            if (!wrapSelectorWheel) {
                if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    internalSetWrapToLinear()
                } else {
                    mPendingWrapToLinear = true
                }
            } else {
                mWrapSelectorWheel = wrapSelectorWheel
                updateWrapStateByContent()
                postInvalidate()
            }
        }
    }

    /**
     * get the "fromValue" by using getValue(), if your picker's minValue is not 0,
     * make sure you can get the accurate value by getValue(), or you can use
     * smoothScrollToValue(int fromValue, int toValue, boolean needRespond)
     * @param toValue the value you want picker to scroll to
     */
    fun smoothScrollToValue(toValue: Int) {
        smoothScrollToValue(getValue(), toValue, true)
    }

    /**
     * get the "fromValue" by using getValue(), if your picker's minValue is not 0,
     * make sure you can get the accurate value by getValue(), or you can use
     * smoothScrollToValue(int fromValue, int toValue, boolean needRespond)
     * @param toValue the value you want picker to scroll to
     * @param needRespond set if you want picker to respond onValueChange listener
     */
    fun smoothScrollToValue(toValue: Int, needRespond: Boolean) {
        smoothScrollToValue(getValue(), toValue, needRespond)
    }

    fun smoothScrollToValue(fromValue: Int, toValue: Int) {
        smoothScrollToValue(fromValue, toValue, true)
    }

    /**
     *
     * @param fromValue need to set the fromValue, can larger than mMaxValue or less than mMinValue
     * @param toValue the value you want picker to scroll to
     * @param needRespond need Respond to the ValueChange callback When Scrolling, default is false
     */
    fun smoothScrollToValue(fromValue: Int, toValue: Int, needRespond: Boolean) {
        var from = fromValue
        var to = toValue
        var deltaIndex: Int
        from = refineValueByLimit(
            from, mMinValue, mMaxValue,
            mWrapSelectorWheel && mWrapSelectorWheelCheck
        )
        to = refineValueByLimit(
            to, mMinValue, mMaxValue,
            mWrapSelectorWheel && mWrapSelectorWheelCheck
        )
        if (mWrapSelectorWheel && mWrapSelectorWheelCheck) {
            deltaIndex = to - from
            val halfOneRecycleSize = getOneRecycleSize() / 2
            if (deltaIndex < -halfOneRecycleSize || halfOneRecycleSize < deltaIndex) {
                deltaIndex =
                    if (deltaIndex > 0) deltaIndex - getOneRecycleSize() else deltaIndex + getOneRecycleSize()
            }
        } else {
            deltaIndex = to - from
        }
        setValue(from)
        if (from == to) {
            return
        }
        scrollByIndexSmoothly(deltaIndex, needRespond)
    }

    /**
     * simplify the "setDisplayedValue() + setMinValue() + setMaxValue()" process,
     * default minValue is 0, and make sure you donot change the minValue.
     * @param display new values to be displayed
     */
    fun refreshByNewDisplayedValues(display: Array<String>?) {
        val minValue = getMinValue()
        val oldMaxValue = getMaxValue()
        val oldSpan = oldMaxValue - minValue + 1
        val newMaxValue = display?.let { it.size - 1 } ?: 0
        val newSpan = newMaxValue - minValue + 1
        if (newSpan > oldSpan) {
            setDisplayedValues(display)
            setMaxValue(newMaxValue)
        } else {
            setMaxValue(newMaxValue)
            setDisplayedValues(display)
        }
    }

    private fun scrollByIndexSmoothly(deltaIndex: Int) {
        scrollByIndexSmoothly(deltaIndex, true)
    }

    /**
     *
     * @param deltaIndex the delta index it will scroll by
     * @param needRespond need Respond to the ValueChange callback When Scrolling, default is false
     */
    private fun scrollByIndexSmoothly(deltaIndex: Int, needRespond: Boolean) {
        var delta = deltaIndex
        if (!(mWrapSelectorWheel && mWrapSelectorWheelCheck)) {
            val willPickRawIndex = getPickedIndexRelativeToRaw()
            if (willPickRawIndex + delta > mMaxShowIndex) {
                delta = mMaxShowIndex - willPickRawIndex
            } else if (willPickRawIndex + delta < mMinShowIndex) {
                delta = mMinShowIndex - willPickRawIndex
            }
        }
        var duration: Int
        var dy: Int
        if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
            //scroll upwords for a distance of less than mItemHeight
            dy = mItemHeight + mCurrDrawFirstItemY
            duration =
                (DEFAULT_INTERVAL_REVISE_DURATION.toFloat() * (mItemHeight + mCurrDrawFirstItemY) / mItemHeight).toInt()
            if (delta < 0) {
                duration = -duration - delta * DEFAULT_INTERVAL_REVISE_DURATION
            } else {
                duration = duration + delta * DEFAULT_INTERVAL_REVISE_DURATION
            }
        } else {
            //scroll downwords for a distance of less than mItemHeight
            dy = mCurrDrawFirstItemY
            duration =
                (DEFAULT_INTERVAL_REVISE_DURATION.toFloat() * (-mCurrDrawFirstItemY) / mItemHeight).toInt()
            if (delta < 0) {
                duration -= delta * DEFAULT_INTERVAL_REVISE_DURATION
            } else {
                duration += delta * DEFAULT_INTERVAL_REVISE_DURATION
            }
        }
        dy += delta * mItemHeight
        if (duration < DEFAULT_MIN_SCROLL_BY_INDEX_DURATION) {
            duration = DEFAULT_MIN_SCROLL_BY_INDEX_DURATION
        }
        if (duration > DEFAULT_MAX_SCROLL_BY_INDEX_DURATION) {
            duration = DEFAULT_MAX_SCROLL_BY_INDEX_DURATION
        }
        mScroller!!.startScroll(0, mCurrDrawGlobalY, 0, dy, duration)
        if (needRespond) {
            mHandler!!.sendMessageDelayed((getMsg(HANDLER_WHAT_REFRESH))!!, (duration / 4).toLong())
        } else {
            mHandler!!.sendMessageDelayed(
                (getMsg(HANDLER_WHAT_REFRESH, 0, 0, needRespond))!!,
                (duration / 4).toLong()
            )
        }
        postInvalidate()
    }

    fun getMinValue(): Int {
        return mMinValue
    }

    fun getMaxValue(): Int {
        return mMaxValue
    }

    fun setMinValue(minValue: Int) {
        mMinValue = minValue
        mMinShowIndex = 0
        updateNotWrapYLimit()
    }

    //compatible for android.widget.NumberPicker
    fun setMaxValue(maxValue: Int) {
        if (mDisplayedValues == null) {
            throw NullPointerException("mDisplayedValues should not be null")
        }
        if (maxValue - mMinValue + 1 > mDisplayedValues!!.size) {
            throw IllegalArgumentException(
                ("(maxValue - mMinValue + 1) should not be larger than mDisplayedValues.length now " +
                        " (maxValue - mMinValue + 1) is " + (maxValue - mMinValue + 1) + " and mDisplayedValues.length is " + mDisplayedValues!!.size)
            )
        }
        mMaxValue = maxValue
        mMaxShowIndex = mMaxValue - mMinValue + mMinShowIndex
        setMinAndMaxShowIndex(mMinShowIndex, mMaxShowIndex)
        updateNotWrapYLimit()
    }

    //compatible for android.widget.NumberPicker
    fun setValue(value: Int) {
        if (value < mMinValue) {
            throw IllegalArgumentException("should not set a value less than mMinValue, value is $value")
        }
        if (value > mMaxValue) {
            throw IllegalArgumentException("should not set a value larger than mMaxValue, value is $value")
        }
        setPickedIndexRelativeToRaw(value - mMinValue)
    }

    //compatible for android.widget.NumberPicker
    fun getValue(): Int {
        return getPickedIndexRelativeToRaw() + mMinValue
    }

    fun getContentByCurrValue(): String? {
        return mDisplayedValues!![getValue() - mMinValue]
    }

    fun getWrapSelectorWheel(): Boolean {
        return mWrapSelectorWheel
    }

    fun getWrapSelectorWheelAbsolutely(): Boolean {
        return mWrapSelectorWheel && mWrapSelectorWheelCheck
    }

    fun setHintText(hintText: String?) {
        if (isStringEqual(mHintText, hintText)) {
            return
        }
        mHintText = hintText
        mTextSizeHintCenterYOffset = getTextCenterYOffset(mPaintHint.fontMetrics)
        mWidthOfHintText = getTextWidth(mHintText, mPaintHint)
        mHandlerLayout!!.sendEmptyMessage(0)
    }

    fun setPickedIndexRelativeToMin(pickedIndexToMin: Int) {
        if (0 <= pickedIndexToMin && pickedIndexToMin < getOneRecycleSize()) {
            mPrivPickedIndex = pickedIndexToMin + mMinShowIndex
            correctPositionByDefaultValue(
                pickedIndexToMin,
                mWrapSelectorWheel && mWrapSelectorWheelCheck
            )
            postInvalidate()
        }
    }

    fun setNormalTextColor(normalTextColor: Int) {
        if (mTextColorNormal == normalTextColor) {
            return
        }
        mTextColorNormal = normalTextColor
        postInvalidate()
    }

    fun setSelectedTextColor(selectedTextColor: Int) {
        if (mTextColorSelected == selectedTextColor) {
            return
        }
        mTextColorSelected = selectedTextColor
        postInvalidate()
    }

    fun setHintTextColor(hintTextColor: Int) {
        if (mTextColorHint == hintTextColor) {
            return
        }
        mTextColorHint = hintTextColor
        mPaintHint.color = mTextColorHint
        postInvalidate()
    }

    fun setDividerColor(dividerColor: Int) {
        if (mDividerColor == dividerColor) {
            return
        }
        mDividerColor = dividerColor
        mPaintDivider.color = mDividerColor
        postInvalidate()
    }

    fun setPickedIndexRelativeToRaw(pickedIndexToRaw: Int) {
        if (mMinShowIndex > -1) {
            if (pickedIndexToRaw in mMinShowIndex..mMaxShowIndex) {
                mPrivPickedIndex = pickedIndexToRaw
                correctPositionByDefaultValue(
                    pickedIndexToRaw - mMinShowIndex,
                    mWrapSelectorWheel && mWrapSelectorWheelCheck
                )
                postInvalidate()
            }
        }
    }

    fun getPickedIndexRelativeToRaw(): Int {
        return if (mCurrDrawFirstItemY != 0) {
            if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
                getWillPickIndexByGlobalY(mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY)
            } else {
                getWillPickIndexByGlobalY(mCurrDrawGlobalY + mCurrDrawFirstItemY)
            }
        } else {
            getWillPickIndexByGlobalY(mCurrDrawGlobalY)
        }
    }

    fun setMinAndMaxShowIndex(minShowIndex: Int, maxShowIndex: Int) {
        setMinAndMaxShowIndex(minShowIndex, maxShowIndex, true)
    }

    fun setMinAndMaxShowIndex(minShowIndex: Int, maxShowIndex: Int, needRefresh: Boolean) {
        if (minShowIndex > maxShowIndex) {
            throw IllegalArgumentException(
                ("minShowIndex should be less than maxShowIndex, minShowIndex is "
                        + minShowIndex + ", maxShowIndex is " + maxShowIndex + ".")
            )
        }
        if (mDisplayedValues == null) {
            throw IllegalArgumentException("mDisplayedValues should not be null, you need to set mDisplayedValues first.")
        } else {
            if (minShowIndex < 0) {
                throw IllegalArgumentException("minShowIndex should not be less than 0, now minShowIndex is $minShowIndex")
            } else if (minShowIndex > mDisplayedValues!!.size - 1) {
                throw IllegalArgumentException(
                    ("minShowIndex should not be larger than (mDisplayedValues.length - 1), now " +
                            "(mDisplayedValues.length - 1) is " + (mDisplayedValues!!.size - 1) + " minShowIndex is " + minShowIndex)
                )
            }
            if (maxShowIndex < 0) {
                throw IllegalArgumentException("maxShowIndex should not be less than 0, now maxShowIndex is $maxShowIndex")
            } else if (maxShowIndex > mDisplayedValues!!.size - 1) {
                throw IllegalArgumentException(
                    ("maxShowIndex should not be larger than (mDisplayedValues.length - 1), now " +
                            "(mDisplayedValues.length - 1) is " + (mDisplayedValues!!.size - 1) + " maxShowIndex is " + maxShowIndex)
                )
            }
        }
        mMinShowIndex = minShowIndex
        mMaxShowIndex = maxShowIndex
        if (needRefresh) {
            mPrivPickedIndex = 0 + mMinShowIndex
            correctPositionByDefaultValue(0, mWrapSelectorWheel && mWrapSelectorWheelCheck)
            postInvalidate()
        }
    }

    /**
     * set the friction of scroller, it will effect the scroller's acceleration when fling
     * @param friction default is ViewConfiguration.get(getContext()).getScrollFriction()
     */
    fun setFriction(friction: Float) {
        if (friction <= 0) {
            throw IllegalArgumentException("you should set a a positive float friction, now friction is $friction")
        }
        mFriction = ViewConfiguration.getScrollFriction() / friction
    }

    //compatible for NumberPicker
    private fun onScrollStateChange(scrollState: Int) {
        if (mScrollState == scrollState) {
            return
        }
        mScrollState = scrollState
        if (mOnScrollListener != null) {
            mOnScrollListener!!.onScrollStateChange(this, scrollState)
        }
    }

    //compatible for NumberPicker
    fun setOnScrollListener(listener: OnScrollListener?) {
        mOnScrollListener = listener
    }

    //compatible for NumberPicker
    fun setOnValueChangedListener(listener: OnValueChangeListener?) {
        mOnValueChangeListener = listener
    }

    fun setOnValueChangedListenerRelativeToRaw(listener: OnValueChangeListenerRelativeToRaw?) {
        mOnValueChangeListenerRaw = listener
    }

    //return index relative to mDisplayedValues from 0.
    private fun getWillPickIndexByGlobalY(globalY: Int): Int {
        if (mItemHeight == 0) {
            return 0
        }
        val willPickIndex = globalY / mItemHeight + mShowCount / 2
        val index = getIndexByRawIndex(
            willPickIndex,
            getOneRecycleSize(),
            mWrapSelectorWheel && mWrapSelectorWheelCheck
        )
        return if (0 <= index && index < getOneRecycleSize()) {
            index + mMinShowIndex
        } else {
            throw IllegalArgumentException(
                ("getWillPickIndexByGlobalY illegal index : " + index
                        + " getOneRecycleSize() : " + getOneRecycleSize() + " mWrapSelectorWheel : " + mWrapSelectorWheel)
            )
        }
    }

    private fun getIndexByRawIndex(index: Int, size: Int, wrap: Boolean): Int {
        var i = index
        if (size <= 0) {
            return 0
        }
        return if (wrap) {
            i %= size
            if (i < 0) {
                i += size
            }
            i
        } else {
            i
        }
    }

    private fun internalSetWrapToLinear() {
        val rawIndex = getPickedIndexRelativeToRaw()
        correctPositionByDefaultValue(rawIndex - mMinShowIndex, false)
        mWrapSelectorWheel = false
        postInvalidate()
    }

    private fun updateDividerAttr() {
        mDividerIndex0 = mShowCount / 2
        mDividerIndex1 = mDividerIndex0 + 1
        dividerY0 = (mDividerIndex0 * mViewHeight / mShowCount).toFloat()
        dividerY1 = (mDividerIndex1 * mViewHeight / mShowCount).toFloat()
        if (mDividerMarginL < 0) {
            mDividerMarginL = 0
        }
        if (mDividerMarginR < 0) {
            mDividerMarginR = 0
        }
        if (mDividerMarginL + mDividerMarginR == 0) {
            return
        }
        if (paddingLeft + mDividerMarginL >= mViewWidth - paddingRight - mDividerMarginR) {
            val surplusMargin =
                paddingLeft + mDividerMarginL + paddingRight + mDividerMarginR - mViewWidth
            mDividerMarginL =
                (mDividerMarginL - surplusMargin.toFloat() * mDividerMarginL / (mDividerMarginL + mDividerMarginR)).toInt()
            mDividerMarginR =
                (mDividerMarginR - surplusMargin.toFloat() * mDividerMarginR / (mDividerMarginL + mDividerMarginR)).toInt()
        }
    }

    private var mNotWrapLimitYTop = 0
    private var mNotWrapLimitYBottom = 0

    private fun updateFontAttr() {
        if (mTextSizeNormal > mItemHeight) {
            mTextSizeNormal = mItemHeight
        }
        if (mTextSizeSelected > mItemHeight) {
            mTextSizeSelected = mItemHeight
        }
        if (mPaintHint == null) {
            throw IllegalArgumentException("mPaintHint should not be null.")
        }
        mPaintHint.textSize = mTextSizeHint.toFloat()
        mTextSizeHintCenterYOffset = getTextCenterYOffset(mPaintHint.fontMetrics)
        mWidthOfHintText = getTextWidth(mHintText, mPaintHint)
        if (mPaintText == null) {
            throw IllegalArgumentException("mPaintText should not be null.")
        }
        mPaintText.textSize = mTextSizeSelected.toFloat()
        mTextSizeSelectedCenterYOffset = getTextCenterYOffset(mPaintText.fontMetrics)
        mPaintText.textSize = mTextSizeNormal.toFloat()
        mTextSizeNormalCenterYOffset = getTextCenterYOffset(mPaintText.fontMetrics)
    }

    private fun updateNotWrapYLimit() {
        mNotWrapLimitYTop = 0
        mNotWrapLimitYBottom = -mShowCount * mItemHeight
        if (mDisplayedValues != null) {
            mNotWrapLimitYTop = (getOneRecycleSize() - (mShowCount / 2) - 1) * mItemHeight
            mNotWrapLimitYBottom = -(mShowCount / 2) * mItemHeight
        }
    }

    private var downYGlobal = 0f
    private var downY = 0f
    private var currY = 0f

    private fun limitY(currDrawGlobalYPreferred: Int): Int {
        var curr = currDrawGlobalYPreferred
        if (mWrapSelectorWheel && mWrapSelectorWheelCheck) {
            return curr
        }
        if (curr < mNotWrapLimitYBottom) {
            curr = mNotWrapLimitYBottom
        } else if (curr > mNotWrapLimitYTop) {
            curr = mNotWrapLimitYTop
        }
        return curr
    }

    private var mFlagMayPress = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mItemHeight == 0) {
            return true
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        currY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mFlagMayPress = true
                mHandler!!.removeMessages(HANDLER_WHAT_REFRESH)
                stopScrolling()
                downY = currY
                downYGlobal = mCurrDrawGlobalY.toFloat()
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
            }
            MotionEvent.ACTION_MOVE -> {
                val spanY = downY - currY
                if (mFlagMayPress && (-mScaledTouchSlop < spanY && spanY < mScaledTouchSlop)) {
                } else {
                    mFlagMayPress = false
                    mCurrDrawGlobalY = limitY((downYGlobal + spanY).toInt())
                    mCurrDrawFirstItemIndex =
                        floor((mCurrDrawGlobalY.toFloat() / mItemHeight).toDouble()).toInt()
                    mCurrDrawFirstItemY =
                        -(mCurrDrawGlobalY - mCurrDrawFirstItemIndex * mItemHeight)
                    invalidate()
                }
                onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
            }
            MotionEvent.ACTION_UP -> if (mFlagMayPress) {
                click(event)
            } else {
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000)
                val velocityY = (velocityTracker.yVelocity * mFriction).toInt()
                if (Math.abs(velocityY) > mMiniVerlocityFling) {
                    mScroller!!.fling(
                        0, mCurrDrawGlobalY, 0, -velocityY, Int.MIN_VALUE, Int.MAX_VALUE, limitY(
                            Int.MIN_VALUE
                        ), limitY(Int.MAX_VALUE)
                    )
                    invalidate()
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                }
                mHandler!!.sendMessageDelayed((getMsg(HANDLER_WHAT_REFRESH))!!, 0)
                releaseVelocityTracker()
            }
            MotionEvent.ACTION_CANCEL -> {
                downYGlobal = mCurrDrawGlobalY.toFloat()
                stopScrolling()
                mHandler!!.sendMessageDelayed((getMsg(HANDLER_WHAT_REFRESH))!!, 0)
            }
        }
        return true
    }

    private fun click(event: MotionEvent) {
        val y = event.y
        for (i in 0 until mShowCount) {
            if (mItemHeight * i <= y && y < mItemHeight * (i + 1)) {
                clickItem(i)
                break
            }
        }
    }

    private fun clickItem(showCountIndex: Int) {
        if (showCountIndex in 0 until mShowCount) {
            //clicked the showCountIndex of the view
            scrollByIndexSmoothly(showCountIndex - mShowCount / 2)
        } else {
            //wrong
        }
    }

    private fun getTextCenterYOffset(fontMetrics: Paint.FontMetrics?): Float {
        return if (fontMetrics == null) {
            0f
        } else abs(fontMetrics.top + fontMetrics.bottom) / 2
    }

    private var mViewWidth = 0
    private var mViewHeight = 0
    private var mItemHeight = 0
    private var dividerY0 = 0f
    private var dividerY1 = 0f
    private var mViewCenterX = 0f

    //defaultPickedIndex relative to the shown part
    private fun correctPositionByDefaultValue(defaultPickedIndex: Int, wrap: Boolean) {
        mCurrDrawFirstItemIndex = defaultPickedIndex - (mShowCount - 1) / 2
        mCurrDrawFirstItemIndex =
            getIndexByRawIndex(mCurrDrawFirstItemIndex, getOneRecycleSize(), wrap)
        if (mItemHeight == 0) {
            mCurrentItemIndexEffect = true
        } else {
            mCurrDrawGlobalY = mCurrDrawFirstItemIndex * mItemHeight
        }
    }

    //first shown item's content index, correspondding to the Index of mDisplayedValued
    private var mCurrDrawFirstItemIndex = 0

    //the first shown item's Y
    private var mCurrDrawFirstItemY = 0

    //global Y conrespondding to scroller
    private var mCurrDrawGlobalY = 0

    override fun computeScroll() {
        if (mItemHeight == 0) {
            return
        }
        if (mScroller!!.computeScrollOffset()) {
            mCurrDrawGlobalY = mScroller!!.currY
            mCurrDrawFirstItemIndex =
                floor((mCurrDrawGlobalY.toFloat() / mItemHeight).toDouble()).toInt()
            mCurrDrawFirstItemY = -(mCurrDrawGlobalY - mCurrDrawFirstItemIndex * mItemHeight)
            postInvalidate()
        }
    }

    private fun releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.clear()
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    private fun updateMaxWHOfDisplayedValues(needRequestLayout: Boolean) {
        updateMaxWidthOfDisplayedValues()
        updateMaxHeightOfDisplayedValues()
        if (needRequestLayout &&
            (mSpecModeW == MeasureSpec.AT_MOST || mSpecModeH == MeasureSpec.AT_MOST)
        ) {
            mHandlerLayout!!.sendEmptyMessage(0)
        }
    }

    private var mSpecModeW = MeasureSpec.UNSPECIFIED
    private var mSpecModeH = MeasureSpec.UNSPECIFIED

    private fun measureWidth(measureSpec: Int): Int {
        var result: Int
        mSpecModeW = MeasureSpec.getMode(measureSpec)
        val specMode = mSpecModeW
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            val marginOfHint =
                if (max(mWidthOfHintText, mWidthOfAlterHint) == 0) 0 else mMarginEndOfHint
            val gapOfHint =
                if (max(mWidthOfHintText, mWidthOfAlterHint) == 0) 0 else mMarginStartOfHint
            val maxWidth = max(
                mMaxWidthOfAlterArrayWithMeasureHint, (
                        max(
                            mMaxWidthOfDisplayedValues,
                            mMaxWidthOfAlterArrayWithoutMeasureHint
                        )
                                + 2 * (gapOfHint + max(
                            mWidthOfHintText,
                            mWidthOfAlterHint
                        ) + marginOfHint + (2 * mItemPaddingHorizental)))
            )
            result = this.paddingLeft + this.paddingRight + maxWidth //MeasureSpec.UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }

    private fun measureHeight(measureSpec: Int): Int {
        var result: Int
        mSpecModeH = MeasureSpec.getMode(measureSpec)
        val specMode = mSpecModeH
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            val maxHeight = mShowCount * (mMaxHeightOfDisplayedValues + 2 * mItemPaddingVertical)
            result = this.paddingTop + this.paddingBottom + maxHeight //MeasureSpec.UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawContent(canvas)
        drawLine(canvas)
        drawHint(canvas)
    }

    private fun drawContent(canvas: Canvas) {
        var index: Int
        var textColor: Int
        var textSize: Float
        var fraction = 0f // fraction of the item in state between normal and selected, in[0, 1]
        var textSizeCenterYOffset: Float
        for (i in 0 until mShowCount + 1) {
            val y = (mCurrDrawFirstItemY + mItemHeight * i).toFloat()
            index = getIndexByRawIndex(
                mCurrDrawFirstItemIndex + i,
                getOneRecycleSize(),
                mWrapSelectorWheel && mWrapSelectorWheelCheck
            )
            if (i == mShowCount / 2) { //this will be picked
                fraction = (mItemHeight + mCurrDrawFirstItemY).toFloat() / mItemHeight
                textColor = getEvaluateColor(fraction, mTextColorNormal, mTextColorSelected)
                textSize = getEvaluateSize(
                    fraction,
                    mTextSizeNormal.toFloat(),
                    mTextSizeSelected.toFloat()
                )
                textSizeCenterYOffset = getEvaluateSize(
                    fraction, mTextSizeNormalCenterYOffset,
                    mTextSizeSelectedCenterYOffset
                )
            } else if (i == mShowCount / 2 + 1) {
                textColor = getEvaluateColor(1 - fraction, mTextColorNormal, mTextColorSelected)
                textSize = getEvaluateSize(
                    1 - fraction,
                    mTextSizeNormal.toFloat(),
                    mTextSizeSelected.toFloat()
                )
                textSizeCenterYOffset = getEvaluateSize(
                    1 - fraction, mTextSizeNormalCenterYOffset,
                    mTextSizeSelectedCenterYOffset
                )
            } else {
                textColor = mTextColorNormal
                textSize = mTextSizeNormal.toFloat()
                textSizeCenterYOffset = mTextSizeNormalCenterYOffset
            }
            mPaintText.color = textColor
            mPaintText.textSize = textSize
            if (0 <= index && index < getOneRecycleSize()) {
                canvas.drawText(
                    mDisplayedValues!![index + mMinShowIndex].toString(), mViewCenterX,
                    y + (mItemHeight / 2) + textSizeCenterYOffset, (mPaintText)
                )
            } else if (!TextUtils.isEmpty(mEmptyItemHint)) {
                canvas.drawText(
                    (mEmptyItemHint)!!, mViewCenterX,
                    y + (mItemHeight / 2) + textSizeCenterYOffset, (mPaintText)
                )
            }
        }
    }

    private fun drawLine(canvas: Canvas) {
        if (mShowDivider) {
            canvas.drawLine(
                (paddingLeft + mDividerMarginL).toFloat(),
                dividerY0,
                (mViewWidth - paddingRight - mDividerMarginR).toFloat(),
                dividerY0,
                mPaintDivider
            )
            canvas.drawLine(
                (paddingLeft + mDividerMarginL).toFloat(),
                dividerY1,
                (mViewWidth - paddingRight - mDividerMarginR).toFloat(),
                dividerY1,
                mPaintDivider
            )
        }
    }

    private fun drawHint(canvas: Canvas) {
        if (TextUtils.isEmpty(mHintText)) {
            return
        }
        canvas.drawText(
            (mHintText)!!,
            mViewCenterX + ((mMaxWidthOfDisplayedValues + mWidthOfHintText) / 2) + mMarginStartOfHint,
            (dividerY0 + dividerY1) / 2 + mTextSizeHintCenterYOffset, (mPaintHint)
        )
    }

    private fun updateMaxWidthOfDisplayedValues() {
        val savedTextSize = mPaintText.textSize
        mPaintText.textSize = mTextSizeSelected.toFloat()
        mMaxWidthOfDisplayedValues = getMaxWidthOfTextArray(mDisplayedValues, mPaintText)
        mMaxWidthOfAlterArrayWithMeasureHint =
            getMaxWidthOfTextArray(mAlterTextArrayWithMeasureHint, mPaintText)
        mMaxWidthOfAlterArrayWithoutMeasureHint =
            getMaxWidthOfTextArray(mAlterTextArrayWithoutMeasureHint, mPaintText)
        mPaintText.textSize = mTextSizeHint.toFloat()
        mWidthOfAlterHint = getTextWidth(mAlterHint, mPaintText)
        mPaintText.textSize = savedTextSize
    }

    private fun <T : CharSequence> getMaxWidthOfTextArray(array: Array<T>?, paint: Paint): Int {
        if (array == null) {
            return 0
        }
        var maxWidth = 0
        for (item: CharSequence? in array) {
            if (item != null) {
                val itemWidth = getTextWidth(item, paint)
                maxWidth = max(itemWidth, maxWidth)
            }
        }
        return maxWidth
    }

    private fun getTextWidth(text: CharSequence?, paint: Paint): Int {
        return if (!TextUtils.isEmpty(text)) {
            (paint.measureText(text.toString()) + 0.5f).toInt()
        } else 0
    }

    private fun updateMaxHeightOfDisplayedValues() {
        val savedTextSize = mPaintText!!.textSize
        mPaintText.textSize = mTextSizeSelected.toFloat()
        mMaxHeightOfDisplayedValues =
            (mPaintText.fontMetrics.bottom - mPaintText.fontMetrics.top + 0.5).toInt()
        mPaintText.textSize = savedTextSize
    }

    private fun updateContentAndIndex(newDisplayedValues: Array<String>?) {
        mMinShowIndex = 0
        mMaxShowIndex = newDisplayedValues?.run { size - 1 } ?: 0
        mDisplayedValues = newDisplayedValues
        updateWrapStateByContent()
    }

    private fun updateContent(newDisplayedValues: Array<String>?) {
        mDisplayedValues = newDisplayedValues
        updateWrapStateByContent()
    }

    //used in setDisplayedValues
    private fun updateValue() {
        inflateDisplayedValuesIfNull()
        updateWrapStateByContent()
        mMinShowIndex = 0
        mMaxShowIndex = mDisplayedValues!!.size - 1
    }

    private fun updateValueForInit() {
        inflateDisplayedValuesIfNull()
        updateWrapStateByContent()
        if (mMinShowIndex == -1) {
            mMinShowIndex = 0
        }
        if (mMaxShowIndex == -1) {
            mMaxShowIndex = mDisplayedValues!!.size - 1
        }
        setMinAndMaxShowIndex(mMinShowIndex, mMaxShowIndex, false)
    }

    private fun inflateDisplayedValuesIfNull() {
        if (mDisplayedValues == null) {
            mDisplayedValues = arrayOf("0")
        }
    }

    private fun updateWrapStateByContent() {
        mWrapSelectorWheelCheck = mDisplayedValues?.run { size > mShowCount } ?: false
    }

    private fun refineValueByLimit(value: Int, minValue: Int, maxValue: Int, wrap: Boolean): Int {
        var va = value
        return if (wrap) {
            if (va > maxValue) {
                va = (va - maxValue) % getOneRecycleSize() + minValue - 1
            } else if (va < minValue) {
                va = ((va - minValue) % getOneRecycleSize()) + maxValue + 1
            }
            va
        } else {
            if (va > maxValue) {
                va = maxValue
            } else if (value < minValue) {
                va = minValue
            }
            va
        }
    }

    private fun stopScrolling() {
        if (mScroller != null) {
            if (!mScroller!!.isFinished) {
                mScroller!!.abortAnimation()
                postInvalidate()
            }
        }
    }

    private fun getMsg(what: Int): Message? {
        return getMsg(what, 0, 0, null)
    }

    private fun getMsg(what: Int, arg1: Int, arg2: Int, obj: Any?): Message? {
        val msg = Message.obtain()
        msg.what = what
        msg.arg1 = arg1
        msg.arg2 = arg2
        msg.obj = obj
        return msg
    }

    //===tool functions===//
    private fun isStringEqual(a: String?, b: String?): Boolean {
        return if (a == null) {
            b == null
        } else {
            (a == b)
        }
    }

    private fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    private fun dp2px(context: Context, dpValue: Float): Int {
        val densityScale = context.resources.displayMetrics.density
        return (dpValue * densityScale + 0.5f).toInt()
    }

    private fun getEvaluateColor(fraction: Float, startColor: Int, endColor: Int): Int {
        val a: Int
        val r: Int
        val g: Int
        val b: Int
        val sA = (startColor and -0x1000000) ushr 24
        val sR = (startColor and 0x00ff0000) ushr 16
        val sG = (startColor and 0x0000ff00) ushr 8
        val sB = (startColor and 0x000000ff) ushr 0
        val eA = (endColor and -0x1000000) ushr 24
        val eR = (endColor and 0x00ff0000) ushr 16
        val eG = (endColor and 0x0000ff00) ushr 8
        val eB = (endColor and 0x000000ff) ushr 0
        a = (sA + (eA - sA) * fraction).toInt()
        r = (sR + (eR - sR) * fraction).toInt()
        g = (sG + (eG - sG) * fraction).toInt()
        b = (sB + (eB - sB) * fraction).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun getEvaluateSize(fraction: Float, startSize: Float, endSize: Float): Float {
        return startSize + (endSize - startSize) * fraction
    }

    private fun convertCharSequenceArrayToStringArray(charSequences: Array<CharSequence>?): Array<String>? {
        if (charSequences == null) {
            return null
        }
        val ret = Array<String>(charSequences.size) { "" }
        for (i in charSequences.indices) {
            ret[i] = charSequences[i].toString()
        }
        return ret
    }
}