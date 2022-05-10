package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo


class CreateCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(displayName: String, checkupType: Int) =
        checkupRepo.createCheckup(displayName, checkupType)
}