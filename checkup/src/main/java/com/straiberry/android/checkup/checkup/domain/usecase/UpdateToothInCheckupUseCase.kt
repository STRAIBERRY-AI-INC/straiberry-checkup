package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo


class UpdateToothInCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int,
        checkupId: String,
        toothId: Int
    ) =
        checkupRepo.updateToothInCheckup(toothNumber, duration, cause, pain, checkupId, toothId)
}