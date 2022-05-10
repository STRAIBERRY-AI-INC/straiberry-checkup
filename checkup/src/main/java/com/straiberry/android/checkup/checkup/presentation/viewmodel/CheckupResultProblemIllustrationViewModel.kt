package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.straiberry.android.common.model.JawPosition

class CheckupResultProblemIllustrationViewModel : ViewModel() {

    private val _submitStateIsListOfProblemsEmpty = MutableLiveData<Boolean>()
    val submitStateIsListOfProblemsEmpty: LiveData<Boolean> =
        _submitStateIsListOfProblemsEmpty

    private val _submitStateToothWithProblemUpperJaw = MutableLiveData<ArrayList<Int>>()
    val submitStateToothWithProblemUpperJaw: LiveData<ArrayList<Int>> =
        _submitStateToothWithProblemUpperJaw

    private val _submitStateToothWithProblemLowerJaw = MutableLiveData<ArrayList<Int>>()
    val submitStateToothWithProblemLowerJaw: LiveData<ArrayList<Int>> =
        _submitStateToothWithProblemLowerJaw


    private val _submitStateToothWithProblemFrontTeethUpper = MutableLiveData<ArrayList<Int>>()
    val submitStateToothWithProblemFrontTeethUpper: LiveData<ArrayList<Int>> =
        _submitStateToothWithProblemFrontTeethUpper

    private val _submitStateToothWithProblemFrontTeethLower = MutableLiveData<ArrayList<Int>>()
    val submitStateToothWithProblemFrontTeethLower: LiveData<ArrayList<Int>> =
        _submitStateToothWithProblemFrontTeethLower

    private val _submitStateCurrentPosition = MutableLiveData<JawPosition?>()
    val submitStateToothCurrentPosition: LiveData<JawPosition?> =
        _submitStateCurrentPosition

    private val _submitStateSwitchBetweenFrontJaws = MutableLiveData<JawPosition?>()
    val submitStateSwitchBetweenFrontJaws: LiveData<JawPosition?> =
        _submitStateSwitchBetweenFrontJaws

    private val _submitStateCurrentFrontTeethPosition = MutableLiveData<Boolean>()
    val submitStateToothCurrentFrontTeethPosition: LiveData<Boolean> =
        _submitStateCurrentFrontTeethPosition

    init {
        _submitStateToothWithProblemUpperJaw.value = ArrayList()
        _submitStateToothWithProblemLowerJaw.value = ArrayList()
        _submitStateToothWithProblemFrontTeethUpper.value = ArrayList()
        _submitStateToothWithProblemFrontTeethLower.value = ArrayList()
        _submitStateCurrentFrontTeethPosition.value = true
        _submitStateIsListOfProblemsEmpty.value = false
    }

    fun setListOfProblemsEmpty(isEmpty: Boolean) {
        _submitStateIsListOfProblemsEmpty.value = isEmpty
    }

    fun switchBetweenFrontJaws(jawPosition: JawPosition) {
        _submitStateSwitchBetweenFrontJaws.value = jawPosition
    }

    fun resetAll() {
        _submitStateToothWithProblemUpperJaw.value = ArrayList()
        _submitStateToothWithProblemLowerJaw.value = ArrayList()
        _submitStateToothWithProblemFrontTeethUpper.value = ArrayList()
        _submitStateToothWithProblemFrontTeethLower.value = ArrayList()
        _submitStateCurrentFrontTeethPosition.value = true
    }

    fun setCurrentPosition(jawPosition: JawPosition) {
        _submitStateCurrentPosition.value = jawPosition
    }

    fun setToothWithProblemsUpperJaw(toothWithProblems: ArrayList<Int>) {
        _submitStateToothWithProblemUpperJaw.value = toothWithProblems
    }

    fun setToothWithProblemsLowerJaw(toothWithProblems: ArrayList<Int>) {
        _submitStateToothWithProblemLowerJaw.value = toothWithProblems
    }

    fun setToothWithProblemsFrontTeethUpper(toothWithProblems: ArrayList<Int>) {
        _submitStateToothWithProblemFrontTeethUpper.value = toothWithProblems
    }

    fun setToothWithProblemsFrontTeethLower(toothWithProblems: ArrayList<Int>) {
        _submitStateToothWithProblemFrontTeethLower.value = toothWithProblems
    }
}