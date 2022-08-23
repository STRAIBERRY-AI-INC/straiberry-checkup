package com.straiberry.android.common.tflite

import android.content.res.AssetManager
import java.io.IOException

object DetectorFactory {
    @Throws(IOException::class)
    fun getDetector(
        assetManager: AssetManager?,
        modelFilename: String
    ): YoloV5Classifier {
        var labelFilename: String? = null
        var isQuantized = false
        var inputSize = 0
        when (modelFilename) {
            BW_DETECTION_MODEL_NAME -> {
                labelFilename = BW_DETECTION_LABEL_FILE
                isQuantized = false
                inputSize = 640
            }
            RGB_JAW_DETECTION_MODEL_NAME -> {
                labelFilename = RGB_JAW_DETECTION_LABEL_FILE
                isQuantized = false
                inputSize = 320
            }
        }
        return YoloV5Classifier.create(
            assetManager!!, modelFilename, labelFilename!!, isQuantized,
            inputSize
        )
    }

    private const val BW_DETECTION_MODEL_NAME = "bwDetectionYolov5s-fp16.tflite"
    private const val BW_DETECTION_LABEL_FILE = "file:///android_asset/bwDetectionLabel.txt"
    private const val RGB_JAW_DETECTION_MODEL_NAME = "yolov5n_320sz_jaw_detection_v3.tflite"
    private const val RGB_JAW_DETECTION_LABEL_FILE =
        "file:///android_asset/rgbJawDetectionLabel.txt"
}

typealias ModelIsInit = (isInit: Boolean) -> Unit
