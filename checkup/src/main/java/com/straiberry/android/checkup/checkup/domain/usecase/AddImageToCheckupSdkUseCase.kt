package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo
import okhttp3.MultipartBody

class AddImageToCheckupSdkUseCase(private val checkupSdkRepo: CheckupSdkRepo) {
    suspend fun execute(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        lastImage: MultipartBody.Part
    ) =
        checkupSdkRepo.addImageToCheckup(checkupId, image, imageType, lastImage)
}