package com.straiberry.android.checkup.checkup.presentation.view.xray

import android.content.Context
import android.graphics.Bitmap
import com.straiberry.android.checkup.checkup.presentation.view.camera.AnalyzeImage
import com.straiberry.android.checkup.checkup.presentation.view.camera.ImageAnalyzer.Companion.MinimumConfidence
import com.straiberry.android.checkup.common.extentions.addPaddingForBetterDetection
import com.straiberry.android.checkup.common.extentions.finalCropOnCapturedXrayJaw
import com.straiberry.android.checkup.common.extentions.resize
import com.straiberry.android.common.tflite.Classifier
import com.straiberry.android.common.tflite.DetectorFactory
import com.straiberry.android.common.tflite.YoloV5Classifier

class OpgAnalyzer(
    val context: Context,
    val opgImage: Bitmap,
    val opgAnalyzerListener: OpgAnalyzerListener
) : AnalyzeImage {
    // Initializing the jaw detection model
    private var detector: YoloV5Classifier = DetectorFactory.getDetector(context.assets, MODEL_NAME)
    var detectedJaw: Classifier.Recognition? = null
    override fun analyze() {

        val cropSize = detector.inputSize
        val resizedBitmap = opgImage.resize(cropSize, cropSize)!!.addPaddingForBetterDetection(32)

        val results: ArrayList<Classifier.Recognition?> = detector.recognizeImage(resizedBitmap)

        val firstOpgDetected =
            results.firstOrNull { it!!.title == JAW_LABEL && it.confidence!! > MinimumConfidence }

        if (firstOpgDetected != null)
            opgAnalyzerListener(
                true,
                opgImage.finalCropOnCapturedXrayJaw(firstOpgDetected.getLocation(), resizedBitmap)
            )
        else
            opgAnalyzerListener(false, null)

        detector.close()
    }

    companion object {
        private const val MODEL_NAME = "bwDetectionYolov5s-fp16.tflite"
        private const val JAW_LABEL = "0"
        private const val BW_LABEL = "1"
    }
}

typealias OpgAnalyzerListener = (isOpg: Boolean, opgImage: Bitmap?) -> Unit