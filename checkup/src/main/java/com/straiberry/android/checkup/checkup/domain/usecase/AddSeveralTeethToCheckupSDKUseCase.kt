package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.data.networking.model.AddToothToCheckupRequest
import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo
import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo


class AddSeveralTeethToCheckupSDKUseCase(private val checkupRepo: CheckupSdkRepo) {
    suspend fun execute(checkupId: String, data: ArrayList<AddToothToCheckupRequest>) =
        checkupRepo.addSeveralToothToCheckup(checkupId, data)
}