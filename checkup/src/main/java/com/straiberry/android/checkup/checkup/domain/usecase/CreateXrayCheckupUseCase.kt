package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.XRayRepo

class CreateXrayCheckupUseCase(private val xRayRepo: XRayRepo) {
    suspend fun execute() = xRayRepo.createCheckup(4)
}