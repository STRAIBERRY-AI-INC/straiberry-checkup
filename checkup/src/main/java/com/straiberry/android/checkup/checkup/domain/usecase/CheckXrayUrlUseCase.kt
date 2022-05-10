package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.XRayRepo

class CheckXrayUrlUseCase(private val xRayRepo: XRayRepo) {
    suspend fun execute(xrayUrl: String) = xRayRepo.checkXrayUrl(xrayUrl)
}