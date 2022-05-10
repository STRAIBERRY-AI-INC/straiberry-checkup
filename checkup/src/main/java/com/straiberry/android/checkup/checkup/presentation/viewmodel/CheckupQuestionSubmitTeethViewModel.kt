package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.data.networking.model.AddToothToCheckupRequest
import com.straiberry.android.checkup.checkup.domain.model.AddToothToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.AddSeveralTeethToCheckupUseCase
import com.straiberry.android.common.base.*
import com.straiberry.android.common.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Submitting all selected teeth by user that has
 * a problem on created checkup.
 */
class CheckupQuestionSubmitTeethViewModel(
    private val addSeveralTeethToCheckupUseCase: AddSeveralTeethToCheckupUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {

    private val _submitStateAddTooth = MutableLiveData<Loadable<AddToothToCheckupSuccessModel>>()
    val submitStateAddTooth: LiveData<Loadable<AddToothToCheckupSuccessModel>> =
        _submitStateAddTooth


    fun addTeethToCheckup(checkupId: String, data: ArrayList<AddToothToCheckupRequest>) {
        _submitStateAddTooth.value = NotLoading
        if (checkupId == "")
            return
        _submitStateAddTooth.value = Loading
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    addSeveralTeethToCheckupUseCase.execute(checkupId, data)
                }
            }.onSuccess {
                _submitStateAddTooth.value = Success(it)
            }.onFailure {
                _submitStateAddTooth.value = Failure(it)
            }
        }
    }
}