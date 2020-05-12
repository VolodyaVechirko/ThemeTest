package com.vvechirko.themetest

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat

class TextDrawable(context: Context, val text: String) : Drawable() {

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            22f, context.resources.displayMetrics
        )
//        isFakeBoldText = true
//        setShadowLayer(6f, 0.0f, 0.0f, Color.BLACK)
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
    }

    private var intrinsicWidth: Int = 0
    private var intrinsicHeight: Int = 0

    var textBounds = Rect()

    init {
        intrinsicWidth = paint.measureText(text).toInt()
        val fm = paint.fontMetrics
        intrinsicHeight = (fm.bottom - fm.top).toInt()

        paint.getTextBounds(text, 0, text.length, textBounds)
    }

    val paintR = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2.0f
    }

    val paintL = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 2.0f
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(bounds, paintR)
        val fm = paint.fontMetrics

        val baseline = bounds.bottom.toFloat() - fm.bottom
        canvas.drawText(text, bounds.left.toFloat(), baseline, paint)

        canvas.drawLine(bounds.left.toFloat(), baseline, bounds.right.toFloat(), baseline, paintL)

        val ascentY = baseline + fm.ascent
        canvas.drawLine(bounds.left.toFloat(), ascentY, bounds.right.toFloat(), ascentY, paintL)

        val descentY = baseline + fm.descent
        canvas.drawLine(bounds.left.toFloat(), descentY, bounds.right.toFloat(), descentY, paintL)

        val topY = baseline + fm.top
        canvas.drawLine(bounds.left.toFloat(), topY, bounds.right.toFloat(), topY, paintL)

        val bottomY = baseline + fm.bottom
        canvas.drawLine(bounds.left.toFloat(), bottomY, bounds.right.toFloat(), bottomY, paintL)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getIntrinsicHeight(): Int {
        return intrinsicHeight
    }

    override fun getIntrinsicWidth(): Int {
        return intrinsicWidth
    }
}