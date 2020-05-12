package com.vvechirko.themetest

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.OverScroller
import android.widget.Toast
import kotlin.math.max

class FlipView(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs),
    ScrollDetector.OnScrollListener {

    private var scrollRange = 0
    private val scaleThreshold = 0.8f

    private val scrollDetector = ScrollDetector(context, this)
    private val scroller = OverScroller(context)

    init {
        isFocusable = true
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
    }

    fun scrollTo(viewIndex: Int, snap: Boolean) {
        val toX = if (viewIndex == 1) scrollRange else 0
        scroller.forceFinished(true)
        val duration = if (snap) 500 else 1000
        scroller.startScroll(scrollX, scrollY, toX - scrollX, 0, duration)
        postInvalidateOnAnimation()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount) {
            getChildAt(i).setOnClickListener {
                Toast.makeText(it.context, "view clicked!", Toast.LENGTH_SHORT).show()
                scrollTo(i, false)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (MeasureSpec.getSize(widthMeasureSpec) * 0.8).toInt()
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val parentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, mode)
        val parentHeightMeasureSpec = heightMeasureSpec

        var minHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams

            val childWidthMeasureSpec = getChildMeasureSpec(
                parentWidthMeasureSpec,
                paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin, lp.width
            )

            val childHeightMeasureSpec = getChildMeasureSpec(
                parentHeightMeasureSpec,
                paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin, lp.height
            )

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)

            minHeight = max(minHeight, child.measuredHeight + lp.topMargin + lp.bottomMargin)
        }

        minHeight = max(minHeight, suggestedMinimumHeight)
        setMeasuredDimension(
            resolveSize(suggestedMinimumWidth, widthMeasureSpec),
            resolveSize(minHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val parentLeft = paddingLeft
        val parentRight = right - left - paddingRight
        val parentTop = paddingTop
        val parentBottom = bottom - top - paddingBottom

        var left = parentLeft
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams
            val childLeft = left + lp.leftMargin
            val childTop = parentTop + lp.topMargin
            val childRight = childLeft + child.measuredWidth
            val childBottom = childTop + child.measuredHeight

            left = childRight + lp.rightMargin
            child.layout(childLeft, childTop, childRight, childBottom)
        }

        val total = left + paddingRight
        scrollRange = if (total > width) total - width else 0
        scaleChildren()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
//        Log.d("onTouchEvent", "onInterceptTouchEvent $event")
        return scrollDetector.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Log.d("onTouchEvent", "onTouchEvent $event")
        return scrollDetector.onTouchEvent(event)
    }

    override fun onScrollStarted(event: MotionEvent) {
        Log.d("ScrollListener", "onScrollStarted")
        scroller.forceFinished(true)
        parent?.requestDisallowInterceptTouchEvent(true)
    }

    override fun onScroll(event: MotionEvent, dx: Int, dy: Int) {
        Log.d("ScrollListener", "onScroll $dx")
        val oldX = scrollX
        val oldY = scrollY

        updateScroll(scrollX + dx)
        onScrollChanged(scrollX, scrollY, oldX, oldY)
    }

    override fun onScrollFinished(event: MotionEvent) {
        Log.d("ScrollListener", "onScrollFinished lastDelta ${scrollDetector.lastDelta}")
        val viewIndex = if (scrollDetector.lastDelta > 0) 1 else 0
        scrollTo(viewIndex, true)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            Log.d("GestureDetector", "computeScroll $currX")

            if (currX != scroller.finalX) {
                updateScroll(currX)
                postInvalidateOnAnimation()
            }
        }
    }

    private fun updateScroll(value: Int) {
        val validValue = when {
            value < 0 -> 0
            value > scrollRange -> scrollRange
            else -> value
        }

        if (scrollX != validValue) {
            scaleChildren()
            scrollX = validValue
        }

        Log.d("GestureDetector", "scrollX $scrollX, scrollRange $scrollRange")
    }

    private fun scaleChildren() {
        val fraction = scrollX / scrollRange.toFloat()
        scaleChild(getChildAt(1), fraction)
        scaleChild(getChildAt(0), 1.0f - fraction)
    }

    private fun scaleChild(child: View, fraction: Float) {
        val scale = scaleThreshold + fraction * (1.0f - scaleThreshold)
        child.scaleX = scale
        child.scaleY = scale
    }

    override fun generateDefaultLayoutParams(): MarginLayoutParams {
        return MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): MarginLayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): MarginLayoutParams {
        return MarginLayoutParams(lp)
    }
}