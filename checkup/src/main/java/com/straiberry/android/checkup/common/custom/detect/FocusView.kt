package com.straiberry.android.checkup.common.custom.detect

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.straiberry.android.checkup.R
import com.straiberry.android.common.extensions.*


class FocusView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attributes, defStyleAttr) {

    private val circleView = CircleView(context)
    private val cardViewLogo = MaterialCardView(context)
    private val cardViewDetectJawType = MaterialCardView(context)
    private val textViewDetectJawType = TextView(context)
    private val imageViewLogo = ImageView(context)
    private val circularProgressIndicator = CircularProgressIndicator(context)
    private lateinit var pulseAnimation: ObjectAnimator
    private var isClear = false

    private var jawDetectedType = "Front Teeth"

    private fun showLogo() {
        removeView(cardViewLogo)
        // Setup card view logo
        addView(cardViewLogo.apply {
            layoutParams = LayoutParams(CardViewLogoStartWidth, CardViewLogoStartHeight)
            elevation = CardViewLogoElevation
            radius = CardViewLogoRadius
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
            x = circleView.getPositionX() - CardViewLogoStartWidth / 2
            y = circleView.getPositionY() - CardViewLogoStartWidth * 4
        })
        cardViewLogo.removeView(imageViewLogo)
        // Setup image view logo
        cardViewLogo.addView(imageViewLogo.apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ImageViewCapturedJawStartHeight
            ).apply { gravity = Gravity.CENTER }
            setImageResource(R.drawable.ic_logo)
        })
    }

    private fun showJawDetectedType() {
        cardViewDetectJawType.visibleWithAnimation()
        // Setup card view jaw detect type
        addView(cardViewDetectJawType.apply {
            layoutParams = LayoutParams(0, CardViewJawTypeHeight)
            radius = CardViewJawTypeRadius
            alpha = CardViewJawTypeAlpha
            x = (circleView.width / 2).toFloat() + cardViewLogo.width / 2
            y = (circleView.height / 2).toFloat() - cardViewLogo.width * 6
            animateDetectedJaw()
        })
        // Setup text view for show the jaw type
        cardViewDetectJawType.addView(textViewDetectJawType.apply {
            visible()
            gravity = Gravity.CENTER
            text = jawDetectedType
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, TextViewJawTypeTextSize)
        })
    }

    private fun animateDetectedJaw() {
        val widthAnimator = ValueAnimator.ofInt(0, CardViewJawTypeWidth)
        widthAnimator.duration = AnimationDuration
        widthAnimator.interpolator = DecelerateInterpolator()
        widthAnimator.addUpdateListener { animation ->
            cardViewDetectJawType.layoutParams.width = animation.animatedValue as Int
            cardViewDetectJawType.requestLayout()
        }
        widthAnimator.start()
    }

    private fun scaleUpLogo() {
        cardViewLogo.animate()
            .alpha(1f)
            .scaleX(1.8f)
            .scaleY(1.8f)
            .translationX((width / 2).toFloat() - cardViewLogo.width / 2)
            .translationY((height / 2).toFloat() - cardViewLogo.width * 6)
            .setDuration(AnimationDuration)
            .withEndAction {
                if (!isClear)
                    showJawDetectedType()
            }
            .setStartDelay(ProgressOnSecondInterval)
            .start()
        imageViewLogo.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setStartDelay(ProgressOnSecondInterval)
            .setDuration(AnimationDuration)
            .start()
    }

    fun scaleUpAndMoveLogoToCenter(endOfPulseAnimation: EndOfAnimation) {
        cardViewDetectJawType.goneWithAnimation()
        cardViewLogo.animate()
            .alpha(1f)
            .scaleX(2.3f)
            .scaleY(2.3f)
            .translationX((width / 2).toFloat() - cardViewLogo.width / 2)
            .translationY((height / 2).toFloat() - cardViewLogo.height)
            .setDuration(AnimationDuration)
            .start()
        imageViewLogo.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(AnimationDuration)
            .start()
        cardViewLogo.animate().alpha(0f)
            .setStartDelay(2000)
            .withEndAction {
                clearView()
                endOfPulseAnimation()
            }
    }

    fun startPulseAnimation(waitForASecond: WaitForASecond) {
        isClear = false
        clearView()
        removeView(circleView)
        addView(circleView)
        circleView.visibleWithAnimation()
        waitForASecond(false)
        pulseAnimation = ObjectAnimator.ofPropertyValuesHolder(
            circleView,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        ).apply {
            duration = AnimationDuration
            repeatCount = PulseAnimationRepeatCount
            repeatMode = ObjectAnimator.REVERSE
            start()
            doOnEnd {
                circleView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .withEndAction {
                        circleView.detectJaw()
                        showWaitProgress(waitForASecond)
                    }
                    .start()
            }
        }
    }

    private fun showWaitProgress(waitForASecond: WaitForASecond) {
        // Show logo
        showLogo()
        // Setup progress
        removeView(circularProgressIndicator)
        addView(circularProgressIndicator.apply {
            visibleWithAnimation()
            // Measure the size of progress and it's must be the same size of
            // border of circle view
            val size = circleView.borderSize.toInt() * 2
            // Set the position to center of circle view
            x = circleView.getPositionX() / 2 - 12.dp
            y = circleView.getPositionY() - size / 2 - 5.dp
            // Set track color to transparent
            trackColor = Color.TRANSPARENT
            indeterminateDrawable!!
                .setTintList(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.primary
                        )
                    )
                )
            trackCornerRadius = TrackerCornerRadios
            indicatorSize = size
        })
        // Adding progress to view
        circleView.detectJawComplete()
        // Start one second progress
        ObjectAnimator.ofInt(circularProgressIndicator, "progress", 0, 100).apply {
            duration = ProgressOnSecondInterval
            startDelay = StartProgressDelay
            start()
            doOnEnd {
                waitForASecond(true)
                scaleUpLogo()
                circularProgressIndicator.hideWithAnimation()
                circleView.hideWithAnimation()
            }
        }
    }

    fun stopPulseAnimation() {
        pulseAnimation.cancel()
        ObjectAnimator.ofPropertyValuesHolder(
            circleView,
            PropertyValuesHolder.ofFloat("scaleX", 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f)
        ).apply {
            duration = StopPulseInterval
            start()
        }
    }


    fun pulseSize(size: Float) {
        circleView.circleSize = size
    }

    fun pulsePosition(xPosition: Float, yPosition: Float) {
        circleView.postInvalidate()
        circleView.circlePositionX = xPosition
        circleView.circlePositionY = yPosition
    }

    fun setJawDetectedType(jawDetectedType: String) {
        this.jawDetectedType = when (jawDetectedType) {
            Front -> FrontTeeth
            Upper -> UpperJaw
            Lower -> LowerJaw
            else -> FrontTeeth
        }
    }


    fun clearView() {
        removeView(circleView)
        removeView(cardViewLogo)
        cardViewLogo.removeView(imageViewLogo)
        removeView(cardViewDetectJawType)
        cardViewDetectJawType.removeView(textViewDetectJawType)
        removeView(circularProgressIndicator)
        isClear = true
        postInvalidate()
        removeAllViews()
        removeAllViewsInLayout()
    }

    companion object {
        private const val StopPulseInterval = 200L
        private const val AnimationDuration = 700L
        private const val CardViewJawTypeRadius = 13f
        private val CardViewJawTypeWidth = 100.dp
        private val CardViewJawTypeHeight = 30.dp
        private const val TextViewJawTypeTextSize = 13f
        private const val CardViewJawTypeAlpha = 0.7f
        private const val ProgressOnSecondInterval = 1000L
        private const val StartProgressDelay = 300L
        private const val CardViewLogoElevation = 10f
        private const val CardViewLogoRadius = 45f

        private val CardViewLogoStartWidth = 32.dp
        private val CardViewLogoStartHeight = 32.dp
        private val ImageViewCapturedJawStartHeight = 200.dp

        private val TrackerCornerRadios = 10.dp
        private const val PulseAnimationRepeatCount = 2

        private const val Front = "front"
        private const val Upper = "upper"
        private const val Lower = "lower"

        private const val FrontTeeth = "Front Teeth"
        private const val UpperJaw = "Upper Jaw"
        private const val LowerJaw = "Lower Jaw"
    }
}

typealias WaitForASecond = (holdingPhoneForASecond: Boolean) -> Unit
typealias EndOfAnimation = () -> Unit
