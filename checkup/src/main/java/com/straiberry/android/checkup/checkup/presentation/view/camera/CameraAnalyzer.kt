package com.straiberry.android.checkup.checkup.presentation.view.camera

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.straiberry.android.checkup.checkup.presentation.viewmodel.Recognition
import com.straiberry.android.checkup.common.extentions.correctRotation
import com.straiberry.android.checkup.common.extentions.toBitmap
import com.straiberry.android.checkup.ml.JawDetection
import org.tensorflow.lite.support.image.TensorImage


/**
 * Analyze the camera frame:
 *
 * 1. Init the detection model
 * 2. Convert image proxy to bitmap and send it to model
 * 3. Get the model outputs
 */
class CameraAnalyzer(
    context: Context,
    private val listener: RecognitionListener
) :
    ImageAnalysis.Analyzer {

    // Initializing the jaw detection model
    private val jawDetectionModel = JawDetection.newInstance(context)

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        val frameAsBitmap = imageProxy.image!!.toBitmap()!!
            .correctRotation(imageProxy.imageInfo.rotationDegrees)

        val tensorImage = TensorImage.fromBitmap(frameAsBitmap)

        // Process the image using the trained model, sort and pick out the top results
        val outputs = jawDetectionModel.process(tensorImage)
            .detectionResultList.take(MAX_RESULT_DISPLAY)

        // Convert the model location to number between 1 and 0
        val left = outputs.first().locationAsRectF.left / imageProxy.width
        val top = outputs.first().locationAsRectF.top / imageProxy.height
        val right = outputs.first().locationAsRectF.right / imageProxy.width
        val bottom = outputs.first().locationAsRectF.bottom / imageProxy.height
        val location = outputs.first().locationAsRectF

        // Check the margin threshold between detected jaw location. This will avoid
        // to detect uncompleted jaw.
        if (left > MARGIN_THRESHOLD &&
            top > MARGIN_THRESHOLD &&
            (1 - right) > MARGIN_THRESHOLD &&
            (1 - bottom) > MARGIN_THRESHOLD
        )
        // Early exit: if prediction is not good enough, don't report it
            if (outputs.first().scoreAsFloat >= MinimumConfidence) {
                listener(
                    Recognition(
                        outputs.first().categoryAsString,
                        outputs.first().scoreAsFloat,
                        location,
                        frameAsBitmap
                    )
                )
            }


        // Close the image,this tells CameraX to feed the next image to the analyzer
        imageProxy.close()
    }


    companion object {
        // Maximum number of results displayed for model
        private const val MAX_RESULT_DISPLAY = 1
        private const val MARGIN_THRESHOLD = 0.06
        const val MinimumConfidence = 0.85
    }
}

// Listener for the result of the camera analyzer
typealias RecognitionListener = (recognition: Recognition) -> Unit
typealias GetTensorImage = (tensorImage: TensorImage) -> Unit