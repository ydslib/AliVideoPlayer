package com.yds.aliyunvideocommon.baseadapter.delegate

import android.util.SparseIntArray
import androidx.annotation.LayoutRes

abstract class MultiTypeDelegate<T> {

    companion object {
        private const val DEFAULT_VIEW_TYPE = -0xff
        const val TYPE_NOT_FOUND = -404
    }


    private var layouts: SparseIntArray? = null
    private var autoMode = false
    private var selfMode = false

    constructor(layouts: SparseIntArray?) {
        this.layouts = layouts
    }

    constructor() {

    }

    fun getDefItemViewType(data: List<T>, position: Int): Int {
        val item: T? = data[position]
        return item?.let { getItemType(it) } ?: DEFAULT_VIEW_TYPE
    }

    /**
     * get the item type from specific entity.
     *
     * @param t entity
     * @return item type
     */
    protected abstract fun getItemType(t: T): Int

    fun getLayoutId(viewType: Int): Int {
        return layouts!![viewType, TYPE_NOT_FOUND]
    }

    private fun addItemType(type: Int, @LayoutRes layoutResId: Int) {
        if (layouts == null) {
            layouts = SparseIntArray()
        }
        layouts!!.put(type, layoutResId)
    }

    /**
     * auto increase type vale, start from 0.
     *
     * @param layoutResIds layout id arrays
     * @return MultiTypeDelegate
     */
    open fun registerItemTypeAutoIncrease(@LayoutRes vararg layoutResIds: Int) = apply {
        autoMode = true
        checkMode(selfMode)
        for (i in layoutResIds.indices) {
            addItemType(i, layoutResIds[i])
        }
    }

    /**
     * set your own type one by one.
     *
     * @param type        type value
     * @param layoutResId layout id
     * @return MultiTypeDelegate
     */
    open fun registerItemType(type: Int, @LayoutRes layoutResId: Int) = apply {
        selfMode = true
        checkMode(autoMode)
        addItemType(type, layoutResId)
    }

    open fun checkMode(mode: Boolean) {
        require(!mode) { "Don't mess two register mode" }
    }
}