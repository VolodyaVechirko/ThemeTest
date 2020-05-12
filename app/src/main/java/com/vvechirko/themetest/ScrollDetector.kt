package com.vvechirko.themetest

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

class ScrollDetector(context: Context, val listener: OnScrollListener) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var isBeingDragged = false
    private var lastMotionX = 0
    private var lastMotionY = 0

    var lastDelta = 0
        private set

    fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE && isBeingDragged) {
            return true
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> run {
                val activePointerId = activePointerId
                if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    return@run
                }
                val pointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(pointerIndex).toInt()
                val xDiff = abs(x - lastMotionX)
                if (xDiff > touchSlop) {
                    isBeingDragged = true
                    lastMotionX = x
                    listener.onScrollStarted(event)
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val x = event.x.toInt()
                lastMotionX = x
                activePointerId = event.getPointerId(0)
                isBeingDragged = false
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                /* Release the drag */
                isBeingDragged = false
                activePointerId = MotionEvent.INVALID_POINTER_ID
                listener.onScrollFinished(event)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                lastMotionX = event.getX(index).toInt()
                activePointerId = event.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                lastMotionX = event.getX(event.findPointerIndex(activePointerId)).toInt()
            }
        }
        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return isBeingDragged
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isBeingDragged = false
                // Remember where the motion event started
                lastMotionX = event.x.toInt()
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(activePointerIndex).toInt()
                var deltaX = lastMotionX - x
                if (!isBeingDragged && abs(deltaX) > touchSlop) {
                    isBeingDragged = true
                    listener.onScrollStarted(event)
                    if (deltaX > 0) {
                        deltaX -= touchSlop
                    } else {
                        deltaX += touchSlop
                    }
                }
                if (isBeingDragged) {
                    // Scroll to follow the motion event
                    lastMotionX = x
                    lastDelta = deltaX
                    listener.onScroll(event, deltaX, 0)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> if (isBeingDragged) {
                activePointerId = MotionEvent.INVALID_POINTER_ID
                isBeingDragged = false
                listener.onScrollFinished(event)
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(event)
        }
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            lastMotionX = ev.getX(newPointerIndex).toInt()
            activePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    interface OnScrollListener {
        fun onScrollStarted(event: MotionEvent)
        fun onScroll(event: MotionEvent, dx: Int, dy: Int)
        fun onScrollFinished(event: MotionEvent)
    }
}