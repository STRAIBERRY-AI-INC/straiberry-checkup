package com.straiberry.android.checkup.checkup.presentation.view.questions

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupGuideTourViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupQuestionBinding
import com.straiberry.android.common.custom.spotlight.OnSpotlightListener
import com.straiberry.android.common.custom.spotlight.ShowCasePosition
import com.straiberry.android.common.custom.spotlight.Spotlight
import com.straiberry.android.common.custom.spotlight.Target
import com.straiberry.android.common.custom.spotlight.shape.Circle
import com.straiberry.android.common.custom.spotlight.shape.Oval
import com.straiberry.android.common.extensions.dp

class CheckupQuestionsSpotLights(
    val binding: FragmentCheckupQuestionBinding,
    val context: Context,
    var spotLight: Spotlight,
    val activity: Activity,
    val guideTourViewModel: CheckupGuideTourViewModel,
) {
    fun setSpotLights() {

        binding.root.doOnPreDraw {

            val selectToothTarget = Target.Builder()
                .setAnchor(binding.selectTeethGuideTour)
                .setShape(
                    Oval(
                        (220).dp(context).toFloat(),
                        (400).dp(context).toFloat(),
                        (200).dp(context).toFloat()
                    )
                )
                .setDescription(context.getString(R.string.tap_here_and_select_a_painful_teeth))
                .showCasePosition(ShowCasePosition.TopCenter)
                .build()


            val answerQuestionsTarget = Target.Builder()
                .setAnchor(binding.spotlightButtonGoTarget)
                .setShape(Circle((20).dp(context).toFloat()))
                .setDescription(context.getString(R.string.tap_here_to_go_to_the_questions))
                .showCasePosition(ShowCasePosition.TopLeft)
                .build()

            val selectAnswerTarget = Target.Builder()
                .setAnchor(binding.spotlightSelectAnswerTarget)
                .setShape(Circle((15).dp(context).toFloat()))
                .setDescription(context.getString(R.string.tap_here_and_select_an_answer))
                .showCasePosition(ShowCasePosition.TopRight)
                .build()


            val doTheCheckupTarget = Target.Builder()
                .setAnchor(binding.spotlightDoTheCheckupTarget)
                .setShape(
                    Oval(
                        (300).dp(context).toFloat(),
                        (90).dp(context).toFloat(),
                        (30).dp(context).toFloat()
                    )
                )
                .setDescription(context.getString(R.string.tap_here_to_continue_checkup_process))
                .showCasePosition(ShowCasePosition.TopCenter)
                .build()

            spotLight = Spotlight.Builder(activity)
                .setTargets(
                    arrayListOf(
                        selectToothTarget,
                        answerQuestionsTarget,
                        selectAnswerTarget,
                        doTheCheckupTarget
                    )
                )
                .setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        com.straiberry.android.common.R.color.primaryOpacity95
                    )
                )
                .setOnSpotlightListener(object : OnSpotlightListener {
                    override fun onSkip() {
                        spotLight.finish()
                        guideTourViewModel.questionGuideTourIsFinished()
                    }
                })
                .build()
            spotLight.start()
        }
    }
}