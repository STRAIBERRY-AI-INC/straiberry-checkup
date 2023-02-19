package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo


class CreateCheckupSdkUseCase(private val checkupSdkRepo: CheckupSdkRepo) {
    suspend fun execute(checkupType: CheckupType) =
        checkupSdkRepo.createCheckup(checkupType)
}