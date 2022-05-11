package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.data.networking.model.AddToothToCheckupRequest
import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo


class AddSeveralTeethToCheckupSdkUseCase(private val checkupSdkRepo: CheckupSdkRepo) {
    suspend fun execute(checkupId: String, data: ArrayList<AddToothToCheckupRequest>) =
        checkupSdkRepo.addSeveralToothToCheckup(checkupId, data)
}