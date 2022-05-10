package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.XRayRepo
import okhttp3.MultipartBody

class AddImageToXrayCheckupUseCase(private val xRayRepo: XRayRepo) {
    suspend fun execute(
        checkupId: MultipartBody.Part,
        image: MultipartBody.Part,
        imageType: MultipartBody.Part,
        lastImage: MultipartBody.Part
    ) = xRayRepo.addImageToCheckup(checkupId, image, imageType, lastImage)
}