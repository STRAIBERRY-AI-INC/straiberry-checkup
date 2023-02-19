package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.domain.model.CheckupHistorySuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.CheckupHistoryUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.CheckupSdkHistoryUseCase
import com.straiberry.android.core.base.*
import com.straiberry.android.core.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Getting checkup history with pagination and update
 * data.
 */
class CheckupHistoryViewModel(
    private val checkupSdkHistoryUseCase: CheckupSdkHistoryUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {

    private val _submitStateCheckupHistory =
        MutableLiveData<Loadable<CheckupHistorySuccessModel>?>()
    val submitStateCheckupHistory: LiveData<Loadable<CheckupHistorySuccessModel>?> =
        _submitStateCheckupHistory

    init {
        _submitStateCheckupHistory.value = null
    }

    fun resetState() {
        _submitStateCheckupHistory.value = NotLoading
    }

    fun checkupHistory(page: Int) {
        _submitStateCheckupHistory.value = NotLoading
        if (page <= 0)
            return
        _submitStateCheckupHistory.value = Loading
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    checkupSdkHistoryUseCase.execute(page = page)
                }
            }.onSuccess {
                _submitStateCheckupHistory.value = Success(it)
            }.onFailure {
                _submitStateCheckupHistory.value = Failure(it)
            }
        }
    }
}