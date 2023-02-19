package com.straiberry.android.checkup.checkup.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.presentation.view.camera.ImageAnalyzer
import com.straiberry.android.checkup.checkup.presentation.view.camera.JawDetector
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.FRONT_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.LOWER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.UPPER_JAW
import com.straiberry.android.checkup.common.extentions.*
import com.straiberry.android.common.model.JawPosition
import com.straiberry.android.common.tflite.Classifier
import com.straiberry.android.common.tflite.YoloV5Classifier
import kotlinx.coroutines.launch

/**
 * Handling all states when a jaw is detected by model.
 * This is includes saving all jaws bitmap and getting all selected jaws by user.
 */
class DetectionJawViewModel : ViewModel() {

    private val _recognitionPosition = MutableLiveData<Recognition>()
    val recognitionPosition: LiveData<Recognition> = _recognitionPosition

    private val _recognitionUpperJaw = MutableLiveData<Bitmap>()
    val recognitionUpperJaw: LiveData<Bitmap> = _recognitionUpperJaw

    private val _recognitionLowerJaw = MutableLiveData<Bitmap>()
    val recognitionLowerJaw: LiveData<Bitmap> = _recognitionLowerJaw

    private val _recognitionFrontJaw = MutableLiveData<Bitmap>()
    val recognitionFrontJaw: LiveData<Bitmap> = _recognitionFrontJaw

    private val _recognitionCurrentCapturedImage = MutableLiveData<Bitmap>()
    val recognitionCurrentCapturedImage: LiveData<Bitmap> = _recognitionCurrentCapturedImage

    private val _stateNextDetectedJaw = MutableLiveData<JawPosition?>()
    val stateNextDetectedJaw: LiveData<JawPosition?> = _stateNextDetectedJaw

    private val _stateCurrentDetectedJaw = MutableLiveData<JawPosition>()
    val stateCurrentDetectedJaw: LiveData<JawPosition> = _stateCurrentDetectedJaw

    private val _stateDetectionModel = MutableLiveData<Boolean>()
    val stateDetectionModel: LiveData<Boolean> = _stateDetectionModel

    private val _statePhotosUploaded = MutableLiveData<Int>()
    val statePhotosUploaded: LiveData<Int> = _statePhotosUploaded

    private val _stateListOfUploadedJaws = MutableLiveData<HashMap<Int, JawPosition>>()
    val stateListOfUploadedJaws: LiveData<HashMap<Int, JawPosition>> = _stateListOfUploadedJaws

    private val _submitStateSelectedJaws = MutableLiveData<HashMap<Int, JawPosition>>()
    val submitStateSelectedJaws: LiveData<HashMap<Int, JawPosition>> = _submitStateSelectedJaws

    private val _submitStateCapturedImageIsCorrect = MutableLiveData<CorrectCapturedImage>()
    val submitStateCapturedImageIsCorrect: LiveData<CorrectCapturedImage> =
        _submitStateCapturedImageIsCorrect

    private val _submitStateDismissCheckupInstruction = MutableLiveData<Boolean>()
    val submitStateDismissCheckupInstruction: LiveData<Boolean> =
        _submitStateDismissCheckupInstruction

    init {
        _submitStateCapturedImageIsCorrect.value = CorrectCapturedImage()
        _submitStateDismissCheckupInstruction.value = false
        _stateListOfUploadedJaws.value = hashMapOf()
        _submitStateSelectedJaws.value = hashMapOf()
        _stateDetectionModel.value = true
        _statePhotosUploaded.value = 0
    }


    fun isImageBlurry(bitmap: Bitmap): Boolean {
        var isImageBlurry = true
        viewModelScope.launch {
            isImageBlurry = bitmap.isBlurry()
        }
        return isImageBlurry
    }

    fun capturedImageIsCorrect(correctCapturedImage: CorrectCapturedImage) {
        _submitStateCapturedImageIsCorrect.postValue(correctCapturedImage)
    }

    fun checkupInstructionHasBeenShowing() {
        _submitStateDismissCheckupInstruction.value = false
    }

    fun checkupInstructionHasBeenDismiss() {
        _submitStateDismissCheckupInstruction.value = true
    }

    fun resetRecognition() {
        _recognitionPosition.postValue(Recognition("", 0f, RectF()))
    }

    // Resting all selected jaws in case of closing checkup or getting back to main page
    fun resetSelectedJaw() {
        _submitStateCapturedImageIsCorrect.value = CorrectCapturedImage()
        _submitStateSelectedJaws.value = hashMapOf()
    }

    // setting all selected jaws by user.
    // 4 means that all jaws is selected.
    fun setSelectedJaw(jawIndex: Int, jawPosition: JawPosition = JawPosition.FrontTeeth) {
        if (jawIndex == AllJawsAreSelected) {
            _submitStateSelectedJaws.value?.put(FRONT_JAW, JawPosition.FrontTeeth)
            _submitStateSelectedJaws.value?.put(UPPER_JAW, JawPosition.UpperJaw)
            _submitStateSelectedJaws.value?.put(LOWER_JAW, JawPosition.LowerJaw)
        } else
            _submitStateSelectedJaws.value?.put(jawIndex, jawPosition)
    }

    // Resting number of uploaded jaws in case of closing checkup or getting back to main page
    fun resetNumberOfUploadedJaw() {
        _stateListOfUploadedJaws.postValue(hashMapOf())
    }

    fun photosUploaded(jawType: JawPosition) {
        _stateListOfUploadedJaws.value?.put(jawType.convertJawToInt(), jawType)
    }

    fun photosUploadedFailed(jawType: JawPosition) {
        _stateListOfUploadedJaws.value?.remove(jawType.convertJawToInt())
    }

    fun updateRecognition(rectPosition: Recognition) {
        _recognitionPosition.postValue(rectPosition)
    }

    fun updateRecognitionUpperJaw(bitmap: Bitmap) {
        _recognitionUpperJaw.postValue(bitmap)
    }

    fun updateRecognitionLowerJaw(bitmap: Bitmap) {
        _recognitionLowerJaw.postValue(bitmap)
    }

    fun updateRecognitionFrontJaw(bitmap: Bitmap) {
        _recognitionFrontJaw.postValue(bitmap)
    }

    fun updateNextDetectedJaw(nextDetectedJaw: JawPosition?) {
        _stateNextDetectedJaw.postValue(nextDetectedJaw)
    }

    fun updateCurrentDetectedJaw(currentDetectedJaw: JawPosition, isMainThread: Boolean = false) {
        _stateCurrentDetectedJaw.value = currentDetectedJaw

    }

    fun updateCurrentCapturedImage(currentCapturedImage: Bitmap) {
        _recognitionCurrentCapturedImage.postValue(currentCapturedImage)
    }

    fun startDetectionModel() {
        _stateDetectionModel.postValue(true)
    }

    fun stopDetectionModel() {
        _stateDetectionModel.postValue(false)
    }


    @androidx.camera.core.ExperimentalGetImage
    fun  detectFromImageProxy(imageProxy: ImageProxy, detector: YoloV5Classifier) {
        val image = imageProxy.image

        val frameAsBitmap = image!!.toBitmap()!!
            .correctRotation(imageProxy.imageInfo.rotationDegrees)
            .cropFromCenter()
            .resize(detector.inputSize, detector.inputSize)

        val results: ArrayList<Classifier.Recognition?> =
            detector.recognizeImage(frameAsBitmap)
        val detectedJaw =
            results.firstOrNull { it!!.confidence!! > ImageAnalyzer.MinimumConfidence }

        // Convert the model location to number between 1 and 0
        if (detectedJaw != null) {
            val left = detectedJaw.getLocation().left / imageProxy.width
            val top = detectedJaw.getLocation().top / imageProxy.height
            val right = detectedJaw.getLocation().right / imageProxy.width
            val bottom =
                detectedJaw.getLocation().bottom / imageProxy.height
            val location = detectedJaw.getLocation()

            // Check the margin threshold between detected jaw location. This will avoid
            // to detect uncompleted jaw.
            if (left > MARGIN_THRESHOLD &&
                top > MARGIN_THRESHOLD &&
                (1 - right) > MARGIN_THRESHOLD &&
                (1 - bottom) > MARGIN_THRESHOLD
            )
            // Early exit: if prediction is not good enough, don't report it
                if (detectedJaw.confidence!! >= JawDetector.MinimumConfidence) {
                    updateRecognition(
                        Recognition(
                            detectedJaw.title!!,
                            detectedJaw.confidence!!,
                            location,
                            frameAsBitmap
                        )
                    )
                }
        }
        imageProxy.close()
    }

    companion object {
        const val MARGIN_THRESHOLD = 0.06
        const val MinimumConfidence = 0.85
        const val DarknessThreshold = 90
        private const val AllJawsAreSelected = 4
    }
}

data class CorrectCapturedImage(
    val isCorrect: Boolean = false,
    val capturedImage: Bitmap? = null,
    val label: JawPosition = JawPosition.FrontTeeth
)

/**
 * Simple Data object with three fields for the label, probability and location of
 * detected jaw.
 */
data class Recognition(
    val label: String,
    val confidence: Float,
    val rectF: RectF,
    val frame: Bitmap? = null
) {
    // For easy logging
    override fun toString(): String {
        return "$label / $probabilityString"
    }

    // Output probability as a string to enable easy data binding
    private val probabilityString = String.format("%.1f%%", confidence * 100.0f)
}