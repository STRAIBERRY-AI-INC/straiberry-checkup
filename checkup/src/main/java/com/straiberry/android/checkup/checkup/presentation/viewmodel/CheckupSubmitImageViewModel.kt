package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.UpdateImageInCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.AddImageToCheckupSdkUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.AddImageToCheckupUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.UpdateImageInCheckupSdkUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.UpdateImageInCheckupUseCase
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.FRONT_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.LOWER_JAW
import com.straiberry.android.checkup.checkup.presentation.view.result.FragmentCheckupResultDetails.Companion.UPPER_JAW
import com.straiberry.android.core.base.*
import com.straiberry.android.core.network.CoroutineContextProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CheckupSubmitImageViewModel(
    private val addImageToCheckupSdkUseCase: AddImageToCheckupSdkUseCase,
    private val updateImageInCheckupSdkUseCase: UpdateImageInCheckupSdkUseCase,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {


    private val _submitStateAddFrontImageToCheckup =
        MutableLiveData<Loadable<AddImageToCheckupSuccessModel>>()
    val submitStateAddFrontImageToCheckup: LiveData<Loadable<AddImageToCheckupSuccessModel>> =
        _submitStateAddFrontImageToCheckup

    fun addFrontImageToCheckup(checkupId: String, image: File, lastImage: Int) {
        val imageFile = MultipartBody.Part.createFormData(
            "image", image.name,
            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val isLastImageMultiPartBody =
            MultipartBody.Part.createFormData("lastimage", lastImage.toString())

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", checkupId)

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", FRONT_JAW.toString())

        _submitStateAddFrontImageToCheckup.postValue(NotLoading)
        if (checkupId == "")
            return
        _submitStateAddFrontImageToCheckup.postValue(Loading)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.main) {
                    addImageToCheckupSdkUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        isLastImageMultiPartBody
                    )
                }
            }.onSuccess {
                _submitStateAddFrontImageToCheckup.postValue(Success(it))
            }.onFailure {
                _submitStateAddFrontImageToCheckup.postValue(Failure(it))
            }
        }
    }

    private val _submitStateAddUpperImageToCheckup =
        MutableLiveData<Loadable<AddImageToCheckupSuccessModel>>()
    val submitStateAddUpperImageToCheckup: LiveData<Loadable<AddImageToCheckupSuccessModel>> =
        _submitStateAddUpperImageToCheckup

    fun addUpperImageToCheckup(checkupId: String, image: File, lastImage: Int) {
        val imageFile = MultipartBody.Part.createFormData(
            "image", image.name,
            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val isLastImageMultiPartBody =
            MultipartBody.Part.createFormData("lastimage", lastImage.toString())

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", checkupId)

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", UPPER_JAW.toString())

        _submitStateAddUpperImageToCheckup.postValue(NotLoading)
        if (checkupId == "")
            return
        _submitStateAddUpperImageToCheckup.postValue(Loading)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.main) {
                    addImageToCheckupSdkUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        isLastImageMultiPartBody
                    )
                }
            }.onSuccess {
                _submitStateAddUpperImageToCheckup.postValue(Success(it))
            }.onFailure {
                _submitStateAddUpperImageToCheckup.postValue(Failure(it))
            }
        }
    }

    private val _submitStateAddLowerImageToCheckup =
        MutableLiveData<Loadable<AddImageToCheckupSuccessModel>>()
    val submitStateAddLowerImageToCheckup: LiveData<Loadable<AddImageToCheckupSuccessModel>> =
        _submitStateAddLowerImageToCheckup

    fun addLowerImageToCheckup(checkupId: String, image: File, lastImage: Int) {
        val imageFile = MultipartBody.Part.createFormData(
            "image", image.name,
            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val isLastImageMultiPartBody =
            MultipartBody.Part.createFormData("lastimage", lastImage.toString())

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", checkupId)

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", LOWER_JAW.toString())

        _submitStateAddLowerImageToCheckup.postValue(NotLoading)
        if (checkupId == "")
            return
        _submitStateAddLowerImageToCheckup.postValue(Loading)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.main) {
                    addImageToCheckupSdkUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        isLastImageMultiPartBody
                    )
                }
            }.onSuccess {
                _submitStateAddLowerImageToCheckup.postValue(Success(it))
            }.onFailure {
                _submitStateAddLowerImageToCheckup.postValue(Failure(it))
            }
        }
    }

    private val _submitStateUpdateFrontImageInCheckup =
        MutableLiveData<Loadable<UpdateImageInCheckupSuccessModel>>()
    val submitStateUpdateFrontImageInCheckup: LiveData<Loadable<UpdateImageInCheckupSuccessModel>> =
        _submitStateUpdateFrontImageInCheckup


    fun updateFrontImageInCheckup(checkupId: String, image: File, imageId: Int) {
        val imageFile = MultipartBody.Part.createFormData(
            "image", image.name,
            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", checkupId)

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", FRONT_JAW.toString())

        _submitStateUpdateFrontImageInCheckup.postValue(NotLoading)
        if (checkupId == "")
            return
        _submitStateUpdateFrontImageInCheckup.postValue(Loading)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.main) {
                    updateImageInCheckupSdkUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        imageId
                    )
                }
            }.onSuccess {
                _submitStateUpdateFrontImageInCheckup.postValue(Success(it))
            }.onFailure {
                _submitStateUpdateFrontImageInCheckup.postValue(Failure(it))
            }
        }
    }

    private val _submitStateUpdateUpperImageInCheckup =
        MutableLiveData<Loadable<UpdateImageInCheckupSuccessModel>>()
    val submitStateUpdateUpperImageInCheckup: LiveData<Loadable<UpdateImageInCheckupSuccessModel>> =
        _submitStateUpdateUpperImageInCheckup


    fun updateUpperImageInCheckup(checkupId: String, image: File, imageId: Int) {
        val imageFile = MultipartBody.Part.createFormData(
            "image", image.name,
            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", checkupId)

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", UPPER_JAW.toString())

        _submitStateUpdateUpperImageInCheckup.postValue(NotLoading)
        if (checkupId == "")
            return
        _submitStateUpdateUpperImageInCheckup.postValue(Loading)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.main) {
                    updateImageInCheckupSdkUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        imageId
                    )
                }
            }.onSuccess {
                _submitStateUpdateUpperImageInCheckup.postValue(Success(it))
            }.onFailure {
                _submitStateUpdateUpperImageInCheckup.postValue(Failure(it))
            }
        }
    }

    private val _submitStateUpdateLowerImageInCheckup =
        MutableLiveData<Loadable<UpdateImageInCheckupSuccessModel>>()
    val submitStateUpdateLowerImageInCheckup: LiveData<Loadable<UpdateImageInCheckupSuccessModel>> =
        _submitStateUpdateLowerImageInCheckup


    fun updateLowerImageInCheckup(checkupId: String, image: File, imageId: Int) {
        val imageFile = MultipartBody.Part.createFormData(
            "image", image.name,
            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", checkupId)

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", LOWER_JAW.toString())

        _submitStateUpdateLowerImageInCheckup.postValue(NotLoading)
        if (checkupId == "")
            return
        _submitStateUpdateLowerImageInCheckup.postValue(Loading)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(contextProvider.main) {
                    updateImageInCheckupSdkUseCase.execute(
                        checkupIdMultipartBody,
                        imageFile,
                        imageTypeMultipartBody,
                        imageId
                    )
                }
            }.onSuccess {
                _submitStateUpdateLowerImageInCheckup.postValue(Success(it))
            }.onFailure {
                _submitStateUpdateLowerImageInCheckup.postValue(Failure(it))
            }
        }
    }


}