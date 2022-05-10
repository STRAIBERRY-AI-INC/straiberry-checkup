package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.UpdateImageInCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.AddImageToCheckupUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.UpdateImageInCheckupUseCase
import com.straiberry.android.common.base.*
import com.straiberry.android.common.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CheckupSubmitImageViewModel(
    private val addImageToCheckupUseCase: AddImageToCheckupUseCase,
    private val updateImageInCheckupUseCase: UpdateImageInCheckupUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {
    private val _submitStateAddImage = MutableLiveData<Loadable<AddImageToCheckupSuccessModel>>()
    val submitStateAddImage: LiveData<Loadable<AddImageToCheckupSuccessModel>> =
        _submitStateAddImage

    private val _submitStateUpdateImage =
        MutableLiveData<Loadable<UpdateImageInCheckupSuccessModel>>()
    val submitStateUpdateImage: LiveData<Loadable<UpdateImageInCheckupSuccessModel>> =
        _submitStateUpdateImage


    fun addImageToCheckup(checkupId: String, image: File, imageType: Int, lastImage: Int) {
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

        _submitStateAddImage.postValue(NotLoading)
        if (checkupId == "")
            return
        _submitStateAddImage.postValue(Loading)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.main) {
                    addImageToCheckupUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        isLastImageMultiPartBody
                    )
                }
            }.onSuccess {
                _submitStateAddImage.postValue(Success(it))
            }.onFailure {
                _submitStateAddImage.postValue(Failure(it))
            }
        }
    }

    fun updateImageInCheckup(checkupId: String, image: File, imageType: Int, imageId: Int) {
        val imageFile = MultipartBody.Part.createFormData(
            "image", image.name,
            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", checkupId)

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", imageType.toString())

        _submitStateUpdateImage.postValue(NotLoading)
        if (checkupId == "")
            return
        _submitStateUpdateImage.postValue(Loading)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.main) {
                    updateImageInCheckupUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        imageId
                    )
                }
            }.onSuccess {
                _submitStateUpdateImage.postValue(Success(it))
            }.onFailure {
                _submitStateUpdateImage.postValue(Failure(it))
            }
        }
    }

}