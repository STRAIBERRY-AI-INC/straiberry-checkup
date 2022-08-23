package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo


class CreateCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(checkupType: Int) =
        checkupRepo.createCheckup(checkupType)
}