package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.AddImageToXrayCheckupUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.AddXrayImageFromUrlUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.CheckXrayUrlUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.CreateXrayCheckupUseCase
import com.straiberry.android.common.base.*
import com.straiberry.android.common.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class XrayViewModel(
    private val createXrayCheckupUseCase: CreateXrayCheckupUseCase,
    private val addImageToXrayCheckupUseCase: AddImageToXrayCheckupUseCase,
    private val addXrayImageFromUrlUseCase: AddXrayImageFromUrlUseCase,
    private val checkXrayUrlUseCase: CheckXrayUrlUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {
    private val _submitStateCheckXrayUrl =
        MutableLiveData<Loadable<Unit>>()
    val submitStateCheckXrayUrl: LiveData<Loadable<Unit>> =
        _submitStateCheckXrayUrl

    fun checkXrayUrl(url: String) {
        _submitStateCheckXrayUrl.value = NotLoading
        if (url.isEmpty())
            return
        _submitStateCheckXrayUrl.value = Loading

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    checkXrayUrlUseCase.execute(url)
                }
            }.onSuccess {
                _submitStateCheckXrayUrl.value = Success(it)
            }.onFailure {
                _submitStateCheckXrayUrl.value = Failure(it)
            }
        }
    }

    private val _submitStateAddXrayImageFromUrl =
        MutableLiveData<Loadable<AddImageToCheckupSuccessModel>>()
    val submitStateAddXrayImageFromUrl: LiveData<Loadable<AddImageToCheckupSuccessModel>> =
        _submitStateAddXrayImageFromUrl

    fun addXrayImageFromUrl(checkupId: String, imageUrl: String) {
        _submitStateAddXrayImageFromUrl.value = NotLoading
        if (checkupId.isEmpty() || imageUrl.isEmpty())
            return
        _submitStateAddXrayImageFromUrl.value = Loading

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    addXrayImageFromUrlUseCase.execute(checkupId, imageUrl)
                }
            }.onSuccess {
                _submitStateAddXrayImageFromUrl.value = Success(it)
            }.onFailure {
                _submitStateAddXrayImageFromUrl.value = Failure(it)
            }
        }
    }

    private val _submitStateUploadXrayImage =
        MutableLiveData<Loadable<AddImageToCheckupSuccessModel>>()
    val submitStateUploadXrayImage: LiveData<Loadable<AddImageToCheckupSuccessModel>> =
        _submitStateUploadXrayImage

    fun uploadXrayImage(
        checkupId: String,
        image: File,
        imageType: String = "2",
        lastImage: String = "1"
    ) {
        _submitStateUploadXrayImage.value = NotLoading

        if (checkupId.isEmpty())
            return

        val imageFile = MultipartBody.Part.createFormData(
            "image", image.name,
            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val isLastImageMultiPartBody =
            MultipartBody.Part.createFormData("lastimage", lastImage.toString())

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", checkupId)

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", imageType.toString())

        _submitStateUploadXrayImage.value = Loading

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.io) {
                    addImageToXrayCheckupUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        isLastImageMultiPartBody
                    )
                }
            }.onSuccess {
                _submitStateUploadXrayImage.value = Success(it)
            }.onFailure {
                _submitStateUploadXrayImage.value = Failure(it)
            }
        }
    }
}