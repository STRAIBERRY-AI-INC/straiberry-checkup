package com.straiberry.android.checkup.common.custom.detect

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.straiberry.android.checkup.R
import com.straiberry.android.common.extensions.dp


class CircleView(
    context: Context,
    attributes: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributes, defStyleAttr) {
    private var mTransparentPaint: Paint? = null
    private var mBorderPaint: Paint? = null
    private val mPath: Path = Path()
    private var mSemiBlackPaint: Paint? = null
    private var removeCircleView = false

    var circleSize = 0f
    var borderSize = 0f
    var circlePositionX = 0f
    var circlePositionY = 0f

    init {
        // Set size of circle
        if (circleSize == 0f) circleSize = DefaultCircleSize
        borderSize = DefaultCircleSize + BorderSize
        // Setup paint for transparent circle
        mTransparentPaint = Paint().apply {
            color = Color.TRANSPARENT
        }
        // Setup paint for circle border
        mBorderPaint = Paint().apply {
            color = ContextCompat.getColor(context, com.straiberry.android.common.R.color.whiteOpacity)
            strokeWidth = StrokeWidth
            style = Paint.Style.STROKE
        }
        // Setup paint for background
        mSemiBlackPaint = Paint().apply {
            color = Color.TRANSPARENT
        }

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPath.reset()
        // Set the positions
        if (circlePositionX == 0f) circlePositionX = (width / 2).toFloat()
        if (circlePositionY == 0f) circlePositionY = (height / 2).toFloat()
        // Draw circle

        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD
        if (!removeCircleView) {
            mPath.addCircle(
                circlePositionX,
                circlePositionY, circleSize, Path.Direction.CW
            )
            canvas.drawCircle(
                circlePositionX,
                circlePositionY, circleSize, mTransparentPaint!!
            )
            // Draw border
            canvas.drawCircle(
                circlePositionX, circlePositionY,
                borderSize, mBorderPaint!!
            )
        }
        // Draw background
        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD
        canvas.drawPath(mPath, mSemiBlackPaint!!)
        canvas.clipPath(mPath)
        canvas.drawColor(
            if (isJawDetected)
                ContextCompat.getColor(context, com.straiberry.android.common.R.color.secondaryLightWithOpacity30)
            else
                ContextCompat.getColor(context, com.straiberry.android.common.R.color.gray200Opacity60)
        )
        canvas.clipPath(mPath)
    }

    fun getPositionX() = circlePositionX
    fun getPositionY() = circlePositionY
    fun detectJaw() {
        postInvalidate()
        isJawDetected = true
    }

    fun detectJawComplete() {
        postInvalidate()
        isJawDetected = false
    }

    companion object {
        private val DefaultCircleSize = (100F).dp
        private val BorderSize = 6.dp
        private val StrokeWidth = (8f).dp
        private var isJawDetected = false
    }
}