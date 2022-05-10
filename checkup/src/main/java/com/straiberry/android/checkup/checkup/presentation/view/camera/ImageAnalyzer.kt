package com.straiberry.android.checkup.checkup.presentation.view.camera

//import com.straiberry.app.common.extensions.convertImageProxyToBitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.straiberry.android.checkup.common.extentions.finalCropOnCapturedJaw
import com.straiberry.android.checkup.ml.JawDetection
import org.tensorflow.lite.support.image.TensorImage

class ImageAnalyzer(
    context: Context,
    private val bitmap: Bitmap,
    private val imageAnalyzerListener: ImageAnalyzerListener
) : AnalyzeImage {

    // Init the model
    private val model = JawDetection.newInstance(context)

    override fun analyze() {

        // Creates inputs for reference.
        val image = TensorImage.fromBitmap(bitmap)

        // Runs model inference and gets result.
        val outputs = model.process(image)

        val detectionResult = outputs.detectionResultList.take(MaxResultDisplay)

        // Gets result from DetectionResult.
        val location = detectionResult.first().locationAsRectF
        val category = detectionResult.first().categoryAsString
        val score = detectionResult.first().scoreAsFloat
        val left = location.left / bitmap.width
        val top = location.top / bitmap.height
        val right = location.right / bitmap.width
        val bottom = location.bottom / bitmap.height

        if (left > MarginThreshold &&
            top > MarginThreshold &&
            (1 - right) > MarginThreshold &&
            (1 - bottom) > MarginThreshold
        ) {
            // Early exit: if prediction is not good enough, don't report it
            // Check for confidence and camera detected label
            if (score >= MinimumConfidence) {
                imageAnalyzerListener(
                    category,
                    true,
                    location,
                    bitmap.finalCropOnCapturedJaw(location)
                )
            } else
                imageAnalyzerListener(category, false, null, null)
        } else
            imageAnalyzerListener(category, false, null, null)


        // Releases model resources if no longer used.
        model.close()
    }

    companion object {
        private const val MaxResultDisplay = 1
        private const val MarginThreshold = 0.06
        const val MinimumConfidence = 0.85
    }
}

interface AnalyzeImage {
    fun analyze()
}
// Listener for the result of the ImageAnalyzer
typealias ImageAnalyzerListener = (label: String, imageIsCorrect: Boolean, location: RectF?, finalCroppedPicture: Bitmap?) -> Unit