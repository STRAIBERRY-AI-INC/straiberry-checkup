package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo


class RemoteUpdateDentalIssueUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(
        toothId: Int,
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int
    ) = checkupRepo.updateDentalIssue(toothId, toothNumber, duration, cause, pain)
}