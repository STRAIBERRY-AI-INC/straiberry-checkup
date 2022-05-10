package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupHistorySuccessResponse
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel

enum class CheckupType { Regular, Whitening, Sensitivity, Treatments, Others, XRays }
class ChooseCheckupTypeViewModel : ViewModel() {

    private val _submitStateSelectedCheckup = MutableLiveData<String>()
    val submitStateSelectedCheckup: LiveData<String> = _submitStateSelectedCheckup

    private val _submitStateSelectedCheckupIndex = MutableLiveData<CheckupType>()
    val submitStateSelectedCheckupIndex: LiveData<CheckupType> = _submitStateSelectedCheckupIndex

    private val _submitStateCheckupResult = MutableLiveData<CheckupResultSuccessModel>()
    val submitStateCheckupResult: LiveData<CheckupResultSuccessModel> = _submitStateCheckupResult


    private val _submitStateUserComeFromCheckupHistory = MutableLiveData<Boolean>()
    val submitStateUserComeFromCheckupHistory: LiveData<Boolean> =
        _submitStateUserComeFromCheckupHistory

    private val _submitStateCreateCheckupId = MutableLiveData<String>()
    val submitStateCreateCheckupId: LiveData<String> =
        _submitStateCreateCheckupId

    // Live data for save question one answer
    private val _submitStateIsComeFromDentalIssue = MutableLiveData<Boolean>()
    val submitStateIsComeFromDentalIssue: LiveData<Boolean> = _submitStateIsComeFromDentalIssue

    init {
        _submitStateUserComeFromCheckupHistory.value = false
    }

    fun resetCheckupResult() {
        _submitStateCheckupResult.value =
            CheckupResultSuccessModel(CheckupHistorySuccessResponse.Data())
    }

    fun userComeFromDentalIssue(isComeFromDentalIssue: Boolean) {
        _submitStateIsComeFromDentalIssue.value = isComeFromDentalIssue
    }

    fun setSelectedCheckup(checkupType: String) {
        _submitStateSelectedCheckup.value = checkupType
    }

    fun setSelectedCheckupIndex(checkupIndex: CheckupType) {
        _submitStateSelectedCheckupIndex.value = checkupIndex
    }

    fun setCheckupId(checkupId: String) {
        _submitStateCreateCheckupId.value = checkupId
    }

    fun setCheckupResult(checkupHistorySuccessModel: CheckupResultSuccessModel) {
        _submitStateCheckupResult.value = checkupHistorySuccessModel
    }
}