package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo


class CreateCheckupSdkUseCase(private val checkupSdkRepo: CheckupSdkRepo) {
    suspend fun execute(checkupType: Int) =
        checkupSdkRepo.createCheckup(checkupType)
}