package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.XRayRepo

class AddXrayImageFromUrlUseCase(private val xRayRepo: XRayRepo) {
    suspend fun execute(checkupId: String, xrayImageUrl: String) =
        xRayRepo.addXrayImage(checkupId, xrayImageUrl)
}