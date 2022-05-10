package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo

class DeleteToothInCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(toothId: Int) = checkupRepo.deleteToothInCheckup(toothId)
}