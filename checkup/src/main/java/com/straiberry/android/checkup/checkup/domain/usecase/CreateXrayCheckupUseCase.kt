package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.repository.XRayRepo

class CreateXrayCheckupUseCase(private val xRayRepo: XRayRepo) {
    suspend fun execute() = xRayRepo.createCheckup(CheckupType.XRays)
}