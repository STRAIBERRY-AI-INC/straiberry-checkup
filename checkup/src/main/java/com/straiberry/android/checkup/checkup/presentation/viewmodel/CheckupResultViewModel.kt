package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.GetCheckupResultUseCase
import com.straiberry.android.common.base.*
import com.straiberry.android.common.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckupResultViewModel(
    private val getCheckupResultUseCase: GetCheckupResultUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {

    private val _submitStateCheckupResult = MutableLiveData<Loadable<CheckupResultSuccessModel>>()
    val submitStateCheckupResult: LiveData<Loadable<CheckupResultSuccessModel>> =
        _submitStateCheckupResult

    fun checkupResult(checkupId: String) {
        _submitStateCheckupResult.value = NotLoading
        if (checkupId == "")
            return
        _submitStateCheckupResult.value = Loading
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    getCheckupResultUseCase.execute(checkupId)
                }
            }.onSuccess {
                _submitStateCheckupResult.value = Success(it)
            }.onFailure {
                _submitStateCheckupResult.value = Failure(it)
            }
        }
    }
}