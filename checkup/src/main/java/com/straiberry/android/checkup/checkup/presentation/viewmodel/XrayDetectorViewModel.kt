package com.straiberry.android.checkup.checkup.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.presentation.view.camera.ImageAnalyzer
import com.straiberry.android.checkup.common.extentions.addPaddingForBetterDetection
import com.straiberry.android.checkup.common.extentions.finalCropOnCapturedXrayJaw
import com.straiberry.android.checkup.common.extentions.resize
import com.straiberry.android.common.tflite.Classifier
import com.straiberry.android.common.tflite.YoloV5Classifier
import kotlinx.coroutines.launch

class XrayDetectorViewModel : ViewModel() {

    fun detectXray(
        detector: YoloV5Classifier,
        opgImage: Bitmap,
        opgAnalyzerListener: OpgAnalyzerListener
    ) {
        viewModelScope.launch {
            val cropSize = detector.inputSize
            val resizedBitmap =
                opgImage.addPaddingForBetterDetection(32).resize(cropSize, cropSize)

            val results: ArrayList<Classifier.Recognition?> = detector.recognizeImage(resizedBitmap)
            val firstOpgDetected =
                results.firstOrNull { it!!.title == JAW_LABEL && it.confidence!! > ImageAnalyzer.MinimumConfidence }

            if (firstOpgDetected != null)
                opgAnalyzerListener(
                    true,
                    opgImage.finalCropOnCapturedXrayJaw(
                        firstOpgDetected.getLocation(),
                        resizedBitmap!!
                    )
                )
            else
                opgAnalyzerListener(false, null)

            detector.close()
        }
    }

    companion object {
        private const val JAW_LABEL = "0"
    }
}
typealias OpgAnalyzerListener = (isOpg: Boolean, opgImage: Bitmap?) -> Unit
