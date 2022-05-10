package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.domain.model.DentalIssueQuestionsModel
import com.straiberry.android.checkup.checkup.domain.usecase.AddDentalIssueUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.DeleteDentalIssueUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.GetAllDentalIssueUseCase
import com.straiberry.android.common.base.Readable
import com.straiberry.android.common.base.ReadableFailure
import com.straiberry.android.common.base.ReadableSuccess
import com.straiberry.android.common.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DentalIssuesViewModel(
    private val addDentalIssueUseCase: AddDentalIssueUseCase,
    private val getAllDentalIssueUseCase: GetAllDentalIssueUseCase,
    private val deleteDentalIssueUseCase: DeleteDentalIssueUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {

    private val _submitStateAddDentalIssue = MutableLiveData<Readable<Unit>>()
    val submitStateAddDentalIssue: LiveData<Readable<Unit>> =
        _submitStateAddDentalIssue

    private val _submitStateGetDentalIssues =
        MutableLiveData<Readable<ArrayList<DentalIssueQuestionsModel>>>()
    val submitStateGetDentalIssues: LiveData<Readable<ArrayList<DentalIssueQuestionsModel>>> =
        _submitStateGetDentalIssues

    private val _submitStateDeleteDentalIssues = MutableLiveData<Readable<Int>>()
    val submitStateDeleteDentalIssues: LiveData<Readable<Int>> =
        _submitStateDeleteDentalIssues

    fun addDentalIssue(dentalIssueQuestionsModel: DentalIssueQuestionsModel) {

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    addDentalIssueUseCase.execute(dentalIssueQuestionsModel)
                }
            }.onSuccess {
                _submitStateAddDentalIssue.value = ReadableSuccess(it)
            }.onFailure {
                _submitStateAddDentalIssue.value = ReadableFailure(it)
            }
        }
    }

    fun getDentalIssues() {
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    getAllDentalIssueUseCase.execute()
                }
            }.onSuccess {
                _submitStateGetDentalIssues.value = ReadableSuccess(it)
            }.onFailure {
                _submitStateGetDentalIssues.value = ReadableFailure(it)
            }
        }
    }

    fun deleteDentalIssues(toothId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    deleteDentalIssueUseCase.execute(toothId)
                }
            }.onSuccess {
                _submitStateDeleteDentalIssues.value = ReadableSuccess(it)
            }.onFailure {
                _submitStateDeleteDentalIssues.value = ReadableFailure(it)
            }
        }
    }
}