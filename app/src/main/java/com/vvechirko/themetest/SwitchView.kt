package com.vvechirko.themetest

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.core.content.ContextCompat

class SwitchView(context: Context, attrs: AttributeSet? = null) : View(context, attrs),
    GestureDetector.OnGestureListener {

    val text1 = "Winner"
    val text2 = "Runner-up"

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            16f, resources.displayMetrics
        )
    }

    private var textBaseline = 0.0f
    private val text1Bounds = Rect()
    private val text2Bounds = Rect()

    val textSelector = ContextCompat.getColorStateList(context, R.color.switch_view_text)!!
    val btnDrawable = ContextCompat.getDrawable(context, R.drawable.swith_view_btn)!!

    private var btnOffset = 0
    private var btnMinOffset = 0
    private var btnMaxOffset = 0

    private var selected: Int = 0

    private val argbEvaluator = ArgbEvaluator()
    private val gestureDetector = GestureDetector(context, this)
    private val scroller = OverScroller(context)
    private var isScrolling = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val hMid = (width - paddingLeft - paddingRight) / 2
        btnMinOffset = 0
        btnMaxOffset = hMid

        btnDrawable.bounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight - btnMaxOffset,
            height - paddingBottom
        )

        val fm = textPaint.fontMetrics
        textBaseline = (height - fm.bottom + fm.top) / 2.0f - fm.top

        text1Bounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight - hMid,
            height - paddingBottom
        )
        text2Bounds.set(
            paddingLeft + hMid,
            paddingTop,
            width - paddingRight,
            height - paddingBottom
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val count = canvas.save()
        canvas.translate(btnOffset.toFloat(), 0.0f)
        btnDrawable.draw(canvas)
        canvas.restoreToCount(count)

        val fraction = btnOffset / btnMaxOffset.toFloat()
        textPaint.color = evaluateTextColor(fraction)
        canvas.drawText(text1, textStart(text1, text1Bounds), textBaseline, textPaint)

        textPaint.color = evaluateTextColor(1.0f - fraction)
        canvas.drawText(text2, textStart(text2, text2Bounds), textBaseline, textPaint)
    }

    private fun textStart(text: String, rect: Rect): Float {
        val textWidth = textPaint.measureText(text)
        return rect.left + (rect.width() - textWidth) / 2.0f
    }

    private fun evaluateTextColor(fraction: Float): Int {
        val defColor = textSelector.defaultColor
        val selectedColor = textSelector.getColorForState(intArrayOf(android.R.attr.state_selected), defColor)
        return argbEvaluator.evaluate(fraction, selectedColor, defColor) as Int
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        log("onTouchEvent $event")
//        return gestureDetector.onTouchEvent(event)
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }

        if (event.action == MotionEvent.ACTION_UP && isScrolling) {
            isScrolling = false
            onScrollFinished()
        }

        return super.onTouchEvent(event)
    }

    private fun onScrollFinished() {
        log("onScrollFinished")
        val halfOffset = (btnMaxOffset - btnMinOffset) / 2
        val toX = if (btnOffset < halfOffset) btnMinOffset else btnMaxOffset

        scroller.forceFinished(true)
        scroller.startScroll(btnOffset, 0, toX - btnOffset, 0, 300)
        postInvalidateOnAnimation()
    }

    override fun onDown(event: MotionEvent): Boolean {
        log("onDown")
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        log("onLongPress")
    }

    override fun onShowPress(event: MotionEvent) {
        log("onShowPress")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        log("onSingleTapUp $x,$y")

        if (text1Bounds.contains(x, y)) select(0)
        else if (text2Bounds.contains(x, y)) select(1)
        return true
    }

    override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        log("onScroll $distanceX, $distanceY")
        isScrolling = true
        val dx = distanceX.toInt()
        updateBtnOffset(btnOffset - dx)
        postInvalidateOnAnimation()
        return true
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        log("onFling $velocityX, $velocityY")
        return false
//        scroller.forceFinished(true)
//        scroller.fling(
//            btnOffset, 0, velocityX.toInt(), velocityY.toInt(),
//            btnMinOffset, btnMaxOffset, 0, 0
//        )
//        postInvalidateOnAnimation()
//        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            log("computeScroll $currX")

            updateBtnOffset(currX)
            if (currX != scroller.finalX) {
                postInvalidateOnAnimation()
            }
        }
    }

    private fun updateBtnOffset(newOffset: Int) {
        log("updateBtnOffset newOffset $newOffset")
        btnOffset = when {
            newOffset > btnMaxOffset -> btnMaxOffset
            newOffset < btnMinOffset -> 0
            else -> newOffset
        }

        var temp = selected
        if (btnOffset == btnMinOffset) {
            temp = 0
        } else if (btnOffset == btnMaxOffset) {
            temp = 1
        }

        if (temp != selected) {
            selected = temp
            log("updateBtnOffset selected $selected")
        }

//        postInvalidateOnAnimation()
    }

    fun select(i: Int) {
        if (selected == i) return
        val toX = if (i == 0) btnMinOffset else btnMaxOffset

        scroller.forceFinished(true)
        scroller.startScroll(btnOffset, 0, toX - btnOffset, 0, 300)
        postInvalidateOnAnimation()
    }

    private fun log(any: Any) = Log.d("SwitchView", any.toString())
}