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
            elevation = CARD_VIEW_LOGO_ELEVATION
            radius = CARD_VIEW_LOGO_RADIUS
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
            radius = CARD_VIEW_JAW_TYPE_RADIUS
            alpha = CARD_VIEW_JAW_TYPE_ALPHA
            x = (circleView.width / 2).toFloat() + cardViewLogo.width / 2
            y = (circleView.height / 2).toFloat() - cardViewLogo.width * 6
            animateDetectedJaw()
        })
        // Setup text view for show the jaw type
        cardViewDetectJawType.addView(textViewDetectJawType.apply {
            visible()
            gravity = Gravity.CENTER
            text = jawDetectedType
            setTextColor(ContextCompat.getColor(context, com.straiberry.android.common.R.color.primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_VIEW_JAW_TYPE_TEXT_SIZE)
        })
    }

    private fun animateDetectedJaw() {
        val widthAnimator = ValueAnimator.ofInt(0, CardViewJawTypeWidth)
        widthAnimator.duration = ANIMATION_DURATION
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
            .setDuration(ANIMATION_DURATION)
            .withEndAction {
                if (!isClear)
                    showJawDetectedType()
            }
            .setStartDelay(ONE_SECOND_PROGRESS_INTERVAL)
            .start()
        imageViewLogo.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setStartDelay(ONE_SECOND_PROGRESS_INTERVAL)
            .setDuration(ANIMATION_DURATION)
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
            .setDuration(ANIMATION_DURATION)
            .start()
        imageViewLogo.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(ANIMATION_DURATION)
            .start()
        cardViewLogo.animate().alpha(0f)
            .setStartDelay(500)
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
            duration = ANIMATION_DURATION
            repeatCount = PULSE_ANIMATION_REPEAT_COUT
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
                            com.straiberry.android.common.R.color.primary
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
            duration = ONE_SECOND_PROGRESS_INTERVAL
            startDelay = START_PROGRESS_DELAY
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
            duration = STOP_PULSE_INTERVAL
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
            FRONT -> FRONT_TEETH_TEXT
            UPPER -> UPPER_JAW_TEXT
            LOWER -> LOWER_JAW_TEXT
            else -> FRONT_TEETH_TEXT
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
        private const val STOP_PULSE_INTERVAL = 100L
        private const val ANIMATION_DURATION = 300L
        private const val CARD_VIEW_JAW_TYPE_RADIUS = 13f
        private val CardViewJawTypeWidth = 100.dp
        private val CardViewJawTypeHeight = 30.dp
        private const val TEXT_VIEW_JAW_TYPE_TEXT_SIZE = 13f
        private const val CARD_VIEW_JAW_TYPE_ALPHA = 0.7f
        private const val ONE_SECOND_PROGRESS_INTERVAL = 1000L
        private const val START_PROGRESS_DELAY = 100L
        private const val CARD_VIEW_LOGO_ELEVATION = 10f
        private const val CARD_VIEW_LOGO_RADIUS = 45f

        private val CardViewLogoStartWidth = 32.dp
        private val CardViewLogoStartHeight = 32.dp
        private val ImageViewCapturedJawStartHeight = 200.dp

        private val TrackerCornerRadios = 10.dp
        private const val PULSE_ANIMATION_REPEAT_COUT = 2

        private const val FRONT = "front"
        private const val UPPER = "upper"
        private const val LOWER = "lower"

        private const val FRONT_TEETH_TEXT = "Front Teeth"
        private const val UPPER_JAW_TEXT = "Upper Jaw"
        private const val LOWER_JAW_TEXT = "Lower Jaw"
    }
}

typealias WaitForASecond = (holdingPhoneForASecond: Boolean) -> Unit
typealias EndOfAnimation = () -> Unit
