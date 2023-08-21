package com.straiberry.android.checkup.checkup.presentation.view.camera

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupGuideTourViewModel
import com.straiberry.android.checkup.databinding.FragmentCameraBinding
import com.straiberry.android.common.custom.spotlight.*
import com.straiberry.android.common.custom.spotlight.Target
import com.straiberry.android.common.custom.spotlight.shape.Circle
import com.straiberry.android.common.extensions.dp

class CameraSpotLights(
    val binding: FragmentCameraBinding,
    val context: Context,
    val activity: Activity,
    val guideTourViewModel: CheckupGuideTourViewModel
) {
    private lateinit var spotLight:Spotlight
    fun setSpotLights() {

        binding.root.doOnPreDraw {
            val uploadPhotosTarget = Target.Builder()
                .setAnchor(binding.imageButtonUpload)
                .setShape(Circle((20).dp(context).toFloat()))
                .setDescription(context.getString(R.string.tap_here_to_upload_tooth_photos))
                .notClickable(false)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onClick() {
                        spotLight.show(1)
                    }

                })
                .showCasePosition(ShowCasePosition.BottomCenter)
                .build()

            val takePhotosTarget = Target.Builder()
                .setAnchor(binding.takePhotoSpotlightTarget)
                .setShape(Circle((20).dp(context).toFloat()))
                .setDescription(context.getString(R.string.you_should_put_your_main_camera_in_front_of_your_mouth_to_detect_and_take_a_photo))
                .notClickable(false)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onClick() {
                        spotLight.finish()
                        guideTourViewModel.cameraGuideTourIsFinished()
                    }
                })
                .showCasePosition(ShowCasePosition.BottomCenter)
                .build()

            spotLight = Spotlight.Builder(activity)
                .setTargets(
                    arrayListOf(
                        uploadPhotosTarget,
                        takePhotosTarget
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
                        guideTourViewModel.cameraGuideTourIsFinished()
                    }
                })
                .build()
            spotLight.start()
        }
    }
}