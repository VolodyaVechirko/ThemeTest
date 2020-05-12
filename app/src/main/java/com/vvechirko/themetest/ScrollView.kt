package com.vvechirko.themetest

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.OverScroller
import kotlin.math.abs

class ScrollView(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    val mOverflingDistance = ViewConfiguration.get(context).scaledOverflingDistance
    val mOverscrollDistance = ViewConfiguration.get(context).scaledOverscrollDistance
    val mScroller = OverScroller(context)

    var mIsBeingDragged = false
    var mLastMotionX = 0
    var mActivePointerId = INVALID_POINTER_ID

    var scrollRange = 0

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true
        }
        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> run {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */
                /*
                * Locally do absolute value. mLastMotionX is set to the x value
                * of the down event.
                */
                val activePointerId = mActivePointerId
                if (activePointerId == INVALID_POINTER_ID) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    return@run
                }
                val pointerIndex = ev.findPointerIndex(activePointerId)
                val x = ev.getX(pointerIndex).toInt()
                if (abs(x - mLastMotionX) > mTouchSlop) {
                    mIsBeingDragged = true
                    mLastMotionX = x
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x.toInt()
                mLastMotionX = x
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = !mScroller.isFinished
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                /* Release the drag */
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER_ID
                if (mScroller.springBack(scrollX, scrollY, 0, scrollRange, 0, 0)) {
                    postInvalidateOnAnimation()
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mLastMotionX = ev.getX(index).toInt()
                mActivePointerId = ev.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId)).toInt()
            }
        }
        /*
        * The only time we want to intercept motion events is if we are in the
        * drag mode.
        */
        return mIsBeingDragged
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                if (childCount == 0) {
                    return false
                }

                mIsBeingDragged = !mScroller.isFinished
                if (mIsBeingDragged) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                // Remember where the motion event started
                mLastMotionX = ev.x.toInt()
                mActivePointerId = ev.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = ev.findPointerIndex(mActivePointerId)
                val x = ev.getX(activePointerIndex).toInt()
                var deltaX = mLastMotionX - x
                if (!mIsBeingDragged && abs(deltaX) > mTouchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    mIsBeingDragged = true
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop
                    } else {
                        deltaX += mTouchSlop
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionX = x
                    val oldX = scrollX
                    val oldY = scrollY
                    overScrollBy(
                        deltaX, 0, scrollX, 0, scrollRange, 0,
                        mOverscrollDistance, 0, true
                    )
                    onScrollChanged(scrollX, scrollY, oldX, oldY)
                }
            }
            MotionEvent.ACTION_UP -> if (mIsBeingDragged) {
                if (mScroller.springBack(scrollX, scrollY, 0, scrollRange, 0, 0)) {
                    postInvalidateOnAnimation()
                }
                mActivePointerId = INVALID_POINTER_ID
                mIsBeingDragged = false
            }
            MotionEvent.ACTION_CANCEL -> if (mIsBeingDragged && childCount > 0) {
                if (mScroller.springBack(scrollX, scrollY, 0, scrollRange, 0, 0)) {
                    postInvalidateOnAnimation()
                }
                mActivePointerId = INVALID_POINTER_ID
                mIsBeingDragged = false
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
        }
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mLastMotionX = ev.getX(newPointerIndex).toInt()
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    override fun onOverScrolled(sX: Int, sY: Int, clampedX: Boolean, clampedY: Boolean) {
        // Treat animating scrolls differently; see #computeScroll() for why.
        if (!mScroller.isFinished) {
            scrollX = sX
            scrollY = sY
//            invalidateParentIfNeeded()
            if (clampedX) {
                mScroller.springBack(scrollX, scrollY, 0, scrollRange, 0, 0)
            }
        } else {
            super.scrollTo(scrollX, scrollY)
        }

        awakenScrollBars()
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            val oldX = scrollX
            val oldY = scrollY
            val x = mScroller.currX
            val y = mScroller.currY

            if (oldX != x || oldY != y) {
                overScrollBy(
                    x - oldX, y - oldY, oldX, oldY, scrollRange, 0,
                    mOverflingDistance, 0, false
                )
                onScrollChanged(scrollX, scrollY, oldX, oldY)
            }
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation()
            }
        }
    }
}