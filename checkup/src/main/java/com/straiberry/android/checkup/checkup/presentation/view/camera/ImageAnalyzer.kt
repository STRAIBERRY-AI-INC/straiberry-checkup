package com.straiberry.android.checkup.checkup.presentation.view.camera

//import com.straiberry.app.common.extensions.convertImageProxyToBitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.straiberry.android.checkup.common.extentions.addPaddingForBetterDetection
import com.straiberry.android.checkup.common.extentions.finalCropOnCapturedJaw
import com.straiberry.android.checkup.common.extentions.resize
import com.straiberry.android.common.tflite.Classifier
import com.straiberry.android.common.tflite.DetectorFactory
import com.straiberry.android.common.tflite.YoloV5Classifier

class ImageAnalyzer(
    context: Context,
    private val bitmap: Bitmap,
    private val checkingFrame: Boolean,
    private val isCheckingImageFromGallery: Boolean = false,
    private val imageAnalyzerListener: ImageAnalyzerListener
) : AnalyzeImage {
    val detector: YoloV5Classifier = DetectorFactory.getDetector(
        context.assets,
        MODEL_NAME
    )

    override fun analyze() {

        val resizedImage = bitmap.resize(detector.inputSize, detector.inputSize)
        val results: ArrayList<Classifier.Recognition?> =
            detector.recognizeImage(resizedImage?.addPaddingForBetterDetection())
        val firstJawDetected =
            results.firstOrNull { it!!.confidence!! >= MinimumConfidence }

        if (firstJawDetected != null) {
            // Gets result from DetectionResult.
            val location = firstJawDetected.getLocation()
            val category = firstJawDetected.title
            val score = firstJawDetected.confidence
            val left = location.left / detector.inputSize
            val top = location.top / detector.inputSize
            val right = location.right / detector.inputSize
            val bottom = location.bottom / detector.inputSize

            if (isCheckingImageFromGallery
                && (left < MarginThreshold ||
                        top < MarginThreshold ||
                        (1 - right) < MarginThreshold ||
                        (1 - bottom) < MarginThreshold)
            )
                imageAnalyzerListener(
                    category!!,
                    score!!,
                    true,
                    location,
                    bitmap
                )
            else if (left > MarginThreshold &&
                top > MarginThreshold &&
                (1 - right) > MarginThreshold &&
                (1 - bottom) > MarginThreshold
            ) {
                imageAnalyzerListener(
                    category!!,
                    score!!,
                    true,
                    location,
                    if (checkingFrame) null else
                        bitmap.finalCropOnCapturedJaw(
                            croppedSize = detector.inputSize,
                            normalizerLocation = location
                        )
                )
            } else
                imageAnalyzerListener("", 0f, false, null, null)
        } else
            imageAnalyzerListener("", 0f, false, null, null)

    }

    companion object {
        private const val MODEL_NAME = "yolov5n_320sz_jaw_detection_v3.tflite"
        private const val MarginThreshold = 0.06
        const val MinimumConfidence = 0.85
    }
}

interface AnalyzeImage {
    fun analyze()
}
// Listener for the result of the ImageAnalyzer
typealias ImageAnalyzerListener = (label: String, score: Float, imageIsCorrect: Boolean, location: RectF?, finalCroppedPicture: Bitmap?) -> Unit