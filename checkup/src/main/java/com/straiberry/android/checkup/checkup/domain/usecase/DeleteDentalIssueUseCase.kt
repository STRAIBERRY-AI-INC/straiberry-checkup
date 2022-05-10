package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupDataStore

class DeleteDentalIssueUseCase(private val checkupDataStore: CheckupDataStore) {
    fun execute(toothId: Int) = checkupDataStore.deleteDentalIssue(toothId)
}