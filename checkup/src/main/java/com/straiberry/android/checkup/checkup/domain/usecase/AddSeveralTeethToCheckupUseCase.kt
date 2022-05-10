package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.data.networking.model.AddToothToCheckupRequest
import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo


class AddSeveralTeethToCheckupUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(checkupId: String, data: ArrayList<AddToothToCheckupRequest>) =
        checkupRepo.addSeveralToothToCheckup(checkupId, data)
}