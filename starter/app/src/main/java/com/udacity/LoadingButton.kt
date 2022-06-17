package com.udacity

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.RESTART
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var text = resources.getString(R.string.button_download)
    private var progress = 0f

    private var buttonText = 0
    private var buttonDefault = 0
    private var buttonLoading = 0
    private var circle = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }

    private var valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 100000
        interpolator = DecelerateInterpolator(10.0f)

        addUpdateListener { updatedAnimation ->
            progress = updatedAnimation.animatedValue as Float
            invalidate()
        }
        repeatMode = RESTART
        repeatCount = INFINITE
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new) {
            ButtonState.Clicked -> {
                progress = 0.0f
                invalidate()
                valueAnimator.cancel()
            }
            ButtonState.Loading -> {
                text = resources.getString(R.string.button_loading)
                invalidate()
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                text = resources.getString(R.string.button_download)
                progress = 0.0f
                invalidate()
                valueAnimator.cancel()
            }
        }
        invalidate()
    }


    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0).apply {
            buttonText = getColor(R.styleable.LoadingButton_text_color, 0)
            buttonDefault = getColor(R.styleable.LoadingButton_background_color, 0)
            buttonLoading = getColor(R.styleable.LoadingButton_loading_background_color, 0)
            circle = getColor(R.styleable.LoadingButton_circle_color, 0)
        }

        buttonState = ButtonState.Completed
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawButton(canvas)
        drawButtonLoading(canvas)
        drawText(canvas)
        drawCircle(canvas)
    }

    private fun drawButton(canvas: Canvas) {
        paint.color = buttonDefault
        canvas.drawRect(0F, height.toFloat(), width.toFloat(), 0F, paint)
    }

    private fun drawText(canvas: Canvas) {
        paint.color = buttonText
        canvas.drawText(text, (widthSize / 2).toFloat(), (height / 2).toFloat() + 30, paint)
    }

    private fun drawButtonLoading(canvas: Canvas) {
        paint.color = buttonLoading
        val progressWidth = progress * widthSize

        canvas.drawRect(0F, height.toFloat(), progressWidth, 0F, paint)
    }

    private fun drawCircle(canvas: Canvas) {
        paint.color = circle

        val progressVal = progress * 360
        val left = widthSize - 200f
        val top = (heightSize / 2) - 50f
        val right = widthSize - 100f
        val bottom = (heightSize / 2) + 50f

        canvas.drawArc(left, top, right, bottom, 0f, progressVal, true, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}