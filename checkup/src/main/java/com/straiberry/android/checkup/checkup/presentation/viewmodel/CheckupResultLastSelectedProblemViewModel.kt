package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Saving the last position of selected problem in each jaw.
 */
class CheckupResultLastSelectedProblemViewModel : ViewModel() {
    private val _submitStateSelectedProblemFrontUpperIllustration = MutableLiveData<Int>()
    val submitStateSelectedProblemFrontUpperIllustration: LiveData<Int> =
        _submitStateSelectedProblemFrontUpperIllustration

    private val _submitStateSelectedProblemFrontLowerIllustration = MutableLiveData<Int>()
    val submitStateSelectedProblemFrontLowerIllustration: LiveData<Int> =
        _submitStateSelectedProblemFrontLowerIllustration

    private val _submitStateSelectedProblemFrontIllustration = MutableLiveData<Int>()
    val submitStateSelectedProblemFrontIllustration: LiveData<Int> =
        _submitStateSelectedProblemFrontIllustration

    private val _submitStateSelectedProblemUpperIllustration = MutableLiveData<Int>()
    val submitStateSelectedProblemUpperIllustration: LiveData<Int> =
        _submitStateSelectedProblemUpperIllustration

    private val _submitStateSelectedProblemLowerIllustration = MutableLiveData<Int>()
    val submitStateSelectedProblemLowerIllustration: LiveData<Int> =
        _submitStateSelectedProblemLowerIllustration

    private val _submitStateSelectedProblemLowerRealImage = MutableLiveData<Int>()
    val submitStateSelectedProblemLowerRealImage: LiveData<Int> =
        _submitStateSelectedProblemLowerRealImage

    private val _submitStateSelectedProblemUpperRealImage = MutableLiveData<Int>()
    val submitStateSelectedProblemUpperRealImage: LiveData<Int> =
        _submitStateSelectedProblemUpperRealImage

    private val _submitStateSelectedProblemFrontRealImage = MutableLiveData<Int>()
    val submitStateSelectedProblemFrontRealImage: LiveData<Int> =
        _submitStateSelectedProblemFrontRealImage

    init {
        _submitStateSelectedProblemFrontUpperIllustration.value = 0
        _submitStateSelectedProblemFrontLowerIllustration.value = 0
        _submitStateSelectedProblemUpperIllustration.value = 0
        _submitStateSelectedProblemLowerIllustration.value = 0
        _submitStateSelectedProblemLowerRealImage.value = 0
        _submitStateSelectedProblemUpperRealImage.value = 0
        _submitStateSelectedProblemFrontRealImage.value = 0
    }

    fun resetAll() {
        _submitStateSelectedProblemFrontUpperIllustration.value = 0
        _submitStateSelectedProblemFrontLowerIllustration.value = 0
        _submitStateSelectedProblemUpperIllustration.value = 0
        _submitStateSelectedProblemLowerIllustration.value = 0
        _submitStateSelectedProblemLowerRealImage.value = 0
        _submitStateSelectedProblemUpperRealImage.value = 0
        _submitStateSelectedProblemFrontRealImage.value = 0
    }

    fun setSelectedProblemFrontUpperIllustration(selectedIndex: Int) {
        _submitStateSelectedProblemFrontUpperIllustration.value = selectedIndex
    }

    fun setSelectedProblemFrontLowerIllustration(selectedIndex: Int) {
        _submitStateSelectedProblemFrontLowerIllustration.value = selectedIndex
    }

    fun setSelectedProblemFrontIllustration(selectedIndex: Int) {
        _submitStateSelectedProblemFrontIllustration.value = selectedIndex
    }

    fun setSelectedProblemUpperIllustration(selectedIndex: Int) {
        _submitStateSelectedProblemUpperIllustration.value = selectedIndex
    }

    fun setSelectedProblemLowerIllustration(selectedIndex: Int) {
        _submitStateSelectedProblemLowerIllustration.value = selectedIndex
    }

    fun setSelectedProblemFrontRealImage(selectedIndex: Int) {
        _submitStateSelectedProblemFrontRealImage.value = selectedIndex
    }

    fun setSelectedProblemUpperRealImage(selectedIndex: Int) {
        _submitStateSelectedProblemUpperRealImage.value = selectedIndex
    }

    fun setSelectedProblemLowerRealImage(selectedIndex: Int) {
        _submitStateSelectedProblemLowerRealImage.value = selectedIndex
    }
}