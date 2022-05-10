package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo
import okhttp3.MultipartBody

class UpdateImageInCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        imageId: Int
    ) =
        checkupRepo.updateImageInCheckup(checkupId, image, imageType, imageId)
}