package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo


class CreateCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(checkupType: CheckupType) =
        checkupRepo.createCheckup(checkupType)
}