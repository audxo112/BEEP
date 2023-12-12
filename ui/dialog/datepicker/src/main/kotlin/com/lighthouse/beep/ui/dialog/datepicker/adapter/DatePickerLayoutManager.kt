package com.lighthouse.beep.ui.dialog.datepicker.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

internal class DatePickerLayoutManager(
    context: Context,
    private val onItemSelectedListener: OnItemSelectedListener? = null,
): LinearLayoutManager(context) {

    private val snapHelper = LinearSnapHelper()
    private var recyclerView: RecyclerView? = null

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        recyclerView = view
        snapHelper.attachToRecyclerView(view)
    }

    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler?) {
        recyclerView = null
        snapHelper.attachToRecyclerView(null)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        applyAlpha()
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return if (orientation == VERTICAL) {
            super.scrollVerticallyBy(dy, recycler, state).also {
                applyAlpha()
            }
        } else {
            0
        }
    }

    private fun applyAlpha() {
        val mid = height / 2f
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val childMid = (getDecoratedTop(child) + getDecoratedBottom(child)) / 2f
            val distanceFromCenter = abs(mid - childMid)
            child.alpha = maxOf((child.height - distanceFromCenter) / child.height, 0.5f)
        }
    }

    private var lastSelectedPosition = -1
    override fun onScrollStateChanged(state: Int) {
        val recyclerView = recyclerView ?: return
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            val mid = height / 2f
            var minDistanceFromCenter = Float.MAX_VALUE
            var position = -1
            for (i in 0 until childCount) {
                val child = getChildAt(i) ?: continue
                val childMid = (getDecoratedTop(child) + getDecoratedBottom(child)) / 2f
                val newDistanceFromCenter = abs(mid - childMid)
                if (minDistanceFromCenter > newDistanceFromCenter) {
                    minDistanceFromCenter = newDistanceFromCenter
                    position = recyclerView.getChildAdapterPosition(child)
                }
            }
            if (position != -1 && lastSelectedPosition != position) {
                lastSelectedPosition = position
                onItemSelectedListener?.onItemSelected(position)
            }
        }
    }
}
