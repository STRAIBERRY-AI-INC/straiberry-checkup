package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.straiberry.android.common.model.JawPosition

/**
 * Handling sates for questions/answers of selecting teeth in checkup
 * questions. This class will save answers for each selected tooth.
 */
class CheckupQuestionViewModel : ViewModel() {
    // Live data for save question one answer
    private val _submitStateAnswerOne = MutableLiveData<HashMap<Int, Int>>()
    val submitStateAnswerOne: LiveData<HashMap<Int, Int>> = _submitStateAnswerOne

    // Live data for checking if question one sub answer is selected
    private val _submitStateAnswerOneIsSubAnswer = MutableLiveData<Boolean>()
    val submitStateAnswerOneIsSubAnswer: LiveData<Boolean> = _submitStateAnswerOneIsSubAnswer

    // Live data for save question two answer
    private val _submitStateAnswerTwo = MutableLiveData<HashMap<Int, Int>>()
    val submitStateAnswerTwo: LiveData<HashMap<Int, Int>> = _submitStateAnswerTwo

    // Live data for checking if question two sub answer is selected
    private val _submitStateAnswerTwoIsSubAnswer = MutableLiveData<Boolean>()
    val submitStateAnswerTwoIsSubAnswer: LiveData<Boolean> = _submitStateAnswerTwoIsSubAnswer

    // Live data for save question three answer
    private val _submitStateAnswerThree = MutableLiveData<HashMap<Int, Int>>()
    val submitStateAnswerThree: LiveData<HashMap<Int, Int>> = _submitStateAnswerThree

    // Live data for save current selected tooth
    private val _submitStateSelectedToothIndex = MutableLiveData<Int>()
    val submitStateSelectedToothIndex: LiveData<Int> = _submitStateSelectedToothIndex

    // Live data for checking if all question is answered
    private val _submitStateIsAllThreeAnswer = MutableLiveData<Int>()
    val submitStateIsAllThreeAnswer: LiveData<Int> = _submitStateIsAllThreeAnswer

    // Figure out witch jaw's are selected
    private val _submitStateSelectedJaw =
        MutableLiveData<HashMap<Int, JawPosition>>()
    val submitStateSelectedJaw: LiveData<HashMap<Int, JawPosition>> =
        _submitStateSelectedJaw


    // Setup the default values
    init {
        _submitStateSelectedJaw.value = hashMapOf()
        _submitStateAnswerOne.value = hashMapOf()
        _submitStateAnswerTwo.value = hashMapOf()
        _submitStateAnswerThree.value = hashMapOf()
        _submitStateAnswerOneIsSubAnswer.value = false
        _submitStateAnswerTwoIsSubAnswer.value = false
    }

    fun selectedJawForCheckup(jawIndex: Int, jawPosition: JawPosition) {
        _submitStateSelectedJaw.value!![jawIndex] = jawPosition
    }

    fun resetSelectedJaw() {
        _submitStateSelectedJaw.value = hashMapOf()
    }

    /**
     * Saving first answer based on tooth index.
     * if user selected one of sub answer's then change value of
     * @see _submitStateAnswerOneIsSubAnswer to true
     */
    fun saveAnswerOne(answerIndex: Int, toothIndex: Int) {
        _submitStateAnswerOneIsSubAnswer.value =
            answerIndex == 0 || answerIndex == 1 || answerIndex == 2
        // User can not select the the sub answer title as an answer
        if (answerIndex != 2)
            _submitStateAnswerOne.value?.put(toothIndex, answerIndex)
    }

    fun getAnswerOne(toothIndex: Int): Int {
        return if (_submitStateAnswerOne.value!![toothIndex]!! < 2)
            _submitStateAnswerOne.value!![toothIndex]!! - 1
        else
            _submitStateAnswerOne.value!![toothIndex]!! - 2
    }

    fun getLocalAnswerOne(toothIndex: Int): Int {
        return _submitStateAnswerOne.value!![toothIndex]!!
    }

    fun getLocalAnswerTwo(toothIndex: Int): Int {
        return _submitStateAnswerTwo.value!![toothIndex]!!
    }

    fun getLocalAnswerThree(toothIndex: Int): Int {
        return _submitStateAnswerThree.value!![toothIndex]!!
    }

    /**
     * Saving second answer based on tooth index.
     * if user selected one of sub answer's then change value of
     * @see _submitStateAnswerTwoIsSubAnswer to true
     */
    fun saveAnswerTwo(answerIndex: Int, toothIndex: Int) {
        _submitStateAnswerTwoIsSubAnswer.value =
            answerIndex == 0 || answerIndex == 1 || answerIndex == 2
        // User can not select the the sub answer title as an answer
        if (answerIndex != 2)
            _submitStateAnswerTwo.value?.put(toothIndex, answerIndex)
    }

    fun getAnswerTwo(toothIndex: Int): Int {
        return if (_submitStateAnswerTwo.value!![toothIndex]!! < 2)
            _submitStateAnswerTwo.value!![toothIndex]!! - 1
        else
            _submitStateAnswerTwo.value!![toothIndex]!! - 2
    }

    /**
     * Saving third answer based on tooth index.
     */
    fun saveAnswerThree(answerIndex: Int, toothIndex: Int) {
        _submitStateAnswerThree.value?.put(toothIndex, answerIndex)
    }

    fun getAnswerThree(toothIndex: Int): Int {
        return _submitStateAnswerThree.value!![toothIndex]!! - 1
    }

    /** If user deletes a tooth then remove all answers */
    fun removeCanceledAnswers(answerIndex: Int) {
        _submitStateAnswerOne.value!!.remove(answerIndex)
        _submitStateAnswerTwo.value!!.remove(answerIndex)
        _submitStateAnswerThree.value!!.remove(answerIndex)
    }

    /** If user cancels the process then reset answer's live data */
    fun removeAllAnswers() {
        _submitStateAnswerOne.value = hashMapOf()
        _submitStateAnswerTwo.value = hashMapOf()
        _submitStateAnswerThree.value = hashMapOf()
    }

    fun resetSubAnswer() {
        _submitStateAnswerTwoIsSubAnswer.value = false
        _submitStateAnswerOneIsSubAnswer.value = false
    }

    /** If User has answered a question then add plus one to
     * @see _submitStateIsAllThreeAnswer
     * */
    fun userAnswersQuestion() {
        _submitStateIsAllThreeAnswer.value = _submitStateIsAllThreeAnswer.value?.plus(1)
    }

    fun resetAnswer() {
        _submitStateIsAllThreeAnswer.value = 0
    }

    fun selectedTooth(toothIndex: Int) {
        _submitStateSelectedToothIndex.value = toothIndex
    }
}