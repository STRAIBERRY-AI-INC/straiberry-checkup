package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo

class RemoteAddDentalIssueUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(
        toothNumber: String,
        duration: Int,
        cause: Int,
        pain: Int
    ) = checkupRepo.addDentalIssue(toothNumber, duration, cause, pain)
}