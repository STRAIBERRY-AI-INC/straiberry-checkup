package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupHistorySuccessResponse

class CheckupHistorySharedViewModel : ViewModel() {
    private val _submitStateCheckupHistory =
        MutableLiveData<ArrayList<CheckupHistorySuccessResponse.Data?>?>()
    val submitStateCheckupHistory: LiveData<ArrayList<CheckupHistorySuccessResponse.Data?>?> =
        _submitStateCheckupHistory


    fun resetSavedCheckupHistory() {
        _submitStateCheckupHistory.value = arrayListOf()
        _submitStateCheckupHistoryLastPage.value = 1
    }

    fun saveLoadedData(checkupHistory: List<CheckupHistorySuccessResponse.Data?>?) {
        if (checkupHistory != null) {
            _submitStateCheckupHistory.value!!.addAll(checkupHistory)
        }
    }

    fun getSavedData() = _submitStateCheckupHistory.value

    private val _submitStateCheckupHistoryLastPage =
        MutableLiveData<Int>()
    val submitStateCheckupHistoryLastPage: LiveData<Int> =
        _submitStateCheckupHistoryLastPage

    fun saveLastPage(lastPage: Int) {
        _submitStateCheckupHistoryLastPage.value = lastPage
    }

    fun getLastSavedPage() = _submitStateCheckupHistoryLastPage.value

    init {
        _submitStateCheckupHistory.value = arrayListOf()
        _submitStateCheckupHistoryLastPage.value = 1
    }
}