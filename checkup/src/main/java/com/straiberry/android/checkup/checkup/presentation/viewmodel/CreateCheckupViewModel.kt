package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.domain.model.CreateCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.SdkTokenSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.CreateCheckupSdkUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.GetSdkTokenUseCase
import com.straiberry.android.core.base.*
import com.straiberry.android.core.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateCheckupViewModel(
    private val createCheckupSdkUseCase: CreateCheckupSdkUseCase,
    private val getSdkTokenUseCase: GetSdkTokenUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {
    private val _submitStateCreateCheckup = MutableLiveData<Loadable<CreateCheckupSuccessModel>>()
    val submitStateCreateCheckup: LiveData<Loadable<CreateCheckupSuccessModel>> =
        _submitStateCreateCheckup

    fun createCheckup(checkupType: Int) {
        _submitStateCreateCheckup.value = NotLoading
        if (checkupType == -2)
            return
        _submitStateCreateCheckup.value = Loading
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    createCheckupSdkUseCase.execute(checkupType)
                }
            }.onSuccess {
                _submitStateCreateCheckup.value = Success(it)
            }.onFailure {
                _submitStateCreateCheckup.value = Failure(it)
            }
        }
    }


    private val _submitStateCheckupSdkToken = MutableLiveData<Loadable<SdkTokenSuccessModel>>()
    val submitStateCheckupSdkToken: LiveData<Loadable<SdkTokenSuccessModel>> =
        _submitStateCheckupSdkToken

    fun getCheckupSdkToken(appId: String, packageName: String) {
        _submitStateCheckupSdkToken.value = NotLoading
        if (appId.isEmpty() || packageName.isEmpty())
            return
        _submitStateCheckupSdkToken.value = Loading
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    getSdkTokenUseCase.execute(appId, packageName)
                }
            }.onSuccess {
                _submitStateCheckupSdkToken.value = Success(it)
            }.onFailure {
                _submitStateCheckupSdkToken.value = Failure(it)
            }
        }
    }
}