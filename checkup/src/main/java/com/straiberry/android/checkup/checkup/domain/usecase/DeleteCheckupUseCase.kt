package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo


class DeleteCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(checkupId: String) = checkupRepo.deleteCheckup(checkupId)
}