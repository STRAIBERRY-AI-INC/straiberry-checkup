package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo

class GetCheckupSdkResultUseCase(private val checkupSdkRepo: CheckupSdkRepo) {
    suspend fun execute(checkupId: String) = checkupSdkRepo.getCheckup(checkupId)
}