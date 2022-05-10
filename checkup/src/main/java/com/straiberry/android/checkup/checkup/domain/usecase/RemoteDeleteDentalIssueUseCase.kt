package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo

class RemoteDeleteDentalIssueUseCase(
    private val checkupRepo: CheckupRepo
) {
    suspend fun execute(dentalIssueId: Int) = checkupRepo.deleteDentalIssue(dentalIssueId)
}