package com.straiberry.android.checkup.checkup.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.FRONT_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.LOWER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.UPPER_JAW
import com.straiberry.android.checkup.common.extentions.convertToJawPosition
import com.straiberry.android.checkup.common.extentions.isBlurry
import com.straiberry.android.common.model.JawPosition
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

    private val _stateNextDetectedJaw = MutableLiveData<String?>()
    val stateNextDetectedJaw: LiveData<String?> = _stateNextDetectedJaw

    private val _stateCurrentDetectedJaw = MutableLiveData<String>()
    val stateCurrentDetectedJaw: LiveData<String> = _stateCurrentDetectedJaw

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

    fun photosUploaded(jawType: Int) {
        _stateListOfUploadedJaws.value?.put(jawType, jawType.convertToJawPosition())
    }

    fun photosUploadedFailed(jawType: Int) {
        _stateListOfUploadedJaws.value?.remove(jawType)
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

    fun updateNextDetectedJaw(nextDetectedJaw: String?) {
        _stateNextDetectedJaw.postValue(nextDetectedJaw)
    }

    fun updateCurrentDetectedJaw(currentDetectedJaw: String, isMainThread: Boolean = false) {
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

    companion object {
        private const val PixelSpacing = 5
        const val DarknessThreshold = 90
        private const val FrontIndex = 0
        private const val UpperIndex = 1
        private const val LowerIndex = 2
        private const val AllJawsAreSelected = 4
    }
}

data class CorrectCapturedImage(
    val isCorrect: Boolean = false,
    val capturedImage: Bitmap? = null,
    val label: String = ""
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