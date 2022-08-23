package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.domain.model.AddToothToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.AddToothToDentalIssueSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.RemoteAddDentalIssueUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.RemoteDeleteDentalIssueUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.RemoteUpdateDentalIssueUseCase
import com.straiberry.android.core.base.*
import com.straiberry.android.core.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemoteDentalIssueViewModel(
    private val remoteAddDentalIssueUseCase: RemoteAddDentalIssueUseCase,
    private val remoteDeleteDentalIssueUseCase: RemoteDeleteDentalIssueUseCase,
    private val remoteUpdateDentalIssueUseCase: RemoteUpdateDentalIssueUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {
    private val _submitStateAddDentalIssue =
        MutableLiveData<Loadable<AddToothToDentalIssueSuccessModel>>()
    val submitStateAddDentalIssue: LiveData<Loadable<AddToothToDentalIssueSuccessModel>> =
        _submitStateAddDentalIssue

    private val _submitStateDeleteDentalIssue =
        MutableLiveData<Loadable<AddToothToCheckupSuccessModel>>()
    val submitStateDeleteDentalIssue: LiveData<Loadable<AddToothToCheckupSuccessModel>> =
        _submitStateDeleteDentalIssue

    private val _submitStateUpdateDentalIssue =
        MutableLiveData<Loadable<AddToothToDentalIssueSuccessModel>>()
    val submitStateUpdateDentalIssue: LiveData<Loadable<AddToothToDentalIssueSuccessModel>> =
        _submitStateUpdateDentalIssue

    fun remoteAddDentalIssue(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int
    ) {
        _submitStateAddDentalIssue.value = NotLoading
        if (toothNumber == "")
            return

        _submitStateAddDentalIssue.value = Loading
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    remoteAddDentalIssueUseCase.execute(toothNumber, duration, cause, pain)
                }
            }.onSuccess {
                _submitStateAddDentalIssue.value = Success(it)
            }.onFailure {
                _submitStateAddDentalIssue.value = Failure(it)
            }
        }
    }

    fun remoteDeleteDentalIssue(
        dentalId: Int
    ) {
        _submitStateDeleteDentalIssue.value = NotLoading
        if (dentalId == -1)
            return

        _submitStateDeleteDentalIssue.value = Loading
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    remoteDeleteDentalIssueUseCase.execute(dentalId)
                }
            }.onSuccess {
                _submitStateDeleteDentalIssue.value = Success(it)
            }.onFailure {
                _submitStateDeleteDentalIssue.value = Failure(it)
            }
        }
    }


    fun remoteUpdateDentalIssue(
        toothId: Int,
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int
    ) {
        _submitStateUpdateDentalIssue.value = NotLoading
        if (toothId == -1)
            return

        _submitStateUpdateDentalIssue.value = Loading
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    remoteUpdateDentalIssueUseCase.execute(
                        toothId,
                        toothNumber,
                        duration,
                        cause,
                        pain
                    )
                }
            }.onSuccess {
                _submitStateUpdateDentalIssue.value = Success(it)
            }.onFailure {
                _submitStateUpdateDentalIssue.value = Failure(it)
            }
        }
    }
}