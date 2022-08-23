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
    private val checkingFrame: Boolean,
    private val isCheckingImageFromGallery: Boolean = false,
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

        if (isCheckingImageFromGallery
            && score >= MinimumConfidence
            && (left < MarginThreshold ||
                    top < MarginThreshold ||
                    (1 - right) < MarginThreshold ||
                    (1 - bottom) < MarginThreshold)
        )
            imageAnalyzerListener(
                category,
                score,
                true,
                location,
                bitmap
            )
        else if (left > MarginThreshold &&
            top > MarginThreshold &&
            (1 - right) > MarginThreshold &&
            (1 - bottom) > MarginThreshold
        ) {
            // Early exit: if prediction is not good enough, don't report it
            // Check for confidence and camera detected label
            if (score >= MinimumConfidence) {
                imageAnalyzerListener(
                    category,
                    score,
                    true,
                    location,
                    if (checkingFrame) null else bitmap.finalCropOnCapturedJaw(location)
                )
            } else
                imageAnalyzerListener(category, score, false, null, null)
        } else
            imageAnalyzerListener(category, score, false, null, null)


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
typealias ImageAnalyzerListener = (label: String, score: Float, imageIsCorrect: Boolean, location: RectF?, finalCroppedPicture: Bitmap?) -> Unit