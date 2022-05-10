package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.model.DentalIssueQuestionsModel
import com.straiberry.android.checkup.checkup.domain.repository.CheckupDataStore


class AddDentalIssueUseCase(private val checkupDataStore: CheckupDataStore) {
    suspend fun execute(dentalIssueQuestionsModel: DentalIssueQuestionsModel) =
        checkupDataStore.saveDentalIssue(dentalIssueQuestionsModel)
}