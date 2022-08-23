package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Saving list of teeth that has problem for all three jaws,
 * The position of a tooth comes with x and y on the result picture.
 */
class CheckupResultProblemRealImageViewModel : ViewModel() {

    private val _submitStateToothWithProblemUpperJaw =
        MutableLiveData<ArrayList<Pair<Double, Double>>>()
    val submitStateToothWithProblemUpperJaw: LiveData<ArrayList<Pair<Double, Double>>> =
        _submitStateToothWithProblemUpperJaw

    private val _submitStateToothWithProblemLowerJaw =
        MutableLiveData<ArrayList<Pair<Double, Double>>>()
    val submitStateToothWithProblemLowerJaw: LiveData<ArrayList<Pair<Double, Double>>> =
        _submitStateToothWithProblemLowerJaw


    private val _submitStateToothWithProblemFrontTeeth =
        MutableLiveData<ArrayList<Pair<Double, Double>>>()
    val submitStateToothWithProblemFrontTeeth: LiveData<ArrayList<Pair<Double, Double>>> =
        _submitStateToothWithProblemFrontTeeth

    fun resetAll() {
        _submitStateToothWithProblemUpperJaw.value = arrayListOf()
        _submitStateToothWithProblemFrontTeeth.value = arrayListOf()
        _submitStateToothWithProblemLowerJaw.value = arrayListOf()
    }

    fun setToothWithProblemsUpperJaw(toothWithProblems: ArrayList<Pair<Double, Double>>) {
        _submitStateToothWithProblemUpperJaw.value = toothWithProblems
    }

    fun setToothWithProblemsLowerJaw(toothWithProblems: ArrayList<Pair<Double, Double>>) {
        _submitStateToothWithProblemLowerJaw.value = toothWithProblems
    }

    fun setToothWithProblemsFrontTeeth(toothWithProblems: ArrayList<Pair<Double, Double>>) {
        _submitStateToothWithProblemFrontTeeth.value = toothWithProblems
    }
}