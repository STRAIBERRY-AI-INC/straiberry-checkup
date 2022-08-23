package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo
import okhttp3.MultipartBody

class UpdateImageInCheckupSdkUseCase(private val checkupSdkRepo: CheckupSdkRepo) {
    suspend fun execute(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        imageId: Int
    ) =
        checkupSdkRepo.updateImageInCheckup(checkupId, image, imageType, imageId)
}