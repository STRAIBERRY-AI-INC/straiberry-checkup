package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo
import okhttp3.MultipartBody

class AddImageToCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        lastImage: MultipartBody.Part
    ) =
        checkupRepo.addImageToCheckup(checkupId, image, imageType, lastImage)
}