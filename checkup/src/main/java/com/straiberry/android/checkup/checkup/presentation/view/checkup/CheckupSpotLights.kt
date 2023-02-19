package com.straiberry.android.checkup.checkup.presentation.view.checkup

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupGuideTourViewModel
import com.straiberry.android.checkup.databinding.FragmentCheckupBinding
import com.straiberry.android.common.custom.spotlight.OnSpotlightListener
import com.straiberry.android.common.custom.spotlight.ShowCasePosition
import com.straiberry.android.common.custom.spotlight.Spotlight
import com.straiberry.android.common.custom.spotlight.Target
import com.straiberry.android.common.custom.spotlight.shape.Circle
import com.straiberry.android.common.extensions.dp

class CheckupSpotLights(
    val binding: FragmentCheckupBinding,
    val context: Context,
    var spotLight: Spotlight,
    val activity: Activity,
    val guideTourViewModel: CheckupGuideTourViewModel
) {
    fun setSpotLights() {

        binding.root.doOnPreDraw {
            val selectCheckupTarget = Target.Builder()
                .setAnchor(binding.radioGroup)
                .setShape(Circle((180).dp(context).toFloat()))
                .setDescription(context.getString(R.string.tap_here_and_select_a_checkup))
                .showCasePosition(ShowCasePosition.TopCenter)
                .build()

            val doCheckupTarget = Target.Builder()
                .setAnchor(binding.targetDoCheckup)
                .setShape(Circle((30).dp(context).toFloat()))
                .setDescription(context.getString(R.string.tap_here_and_run_checkup))
                .showCasePosition(ShowCasePosition.TopLeft)
                .build()

            spotLight = Spotlight.Builder(activity)
                .setTargets(arrayListOf(selectCheckupTarget, doCheckupTarget))
                .setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        com.straiberry.android.common.R.color.primaryOpacity95
                    )
                )
                .setOnSpotlightListener(object : OnSpotlightListener {
                    override fun onSkip() {
                        spotLight.finish()
                        guideTourViewModel.checkupGuideTourIsFinished()
                    }
                })
                .build()
            spotLight.start()
        }
    }
}