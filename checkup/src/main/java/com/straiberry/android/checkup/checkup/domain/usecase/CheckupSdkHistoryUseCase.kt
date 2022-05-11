package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.model.CheckupHistorySuccessModel
import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo


class CheckupSdkHistoryUseCase(private val checkupSdkRepo: CheckupSdkRepo) {
    suspend fun execute(page: Int): CheckupHistorySuccessModel =
        checkupSdkRepo.checkupHistory(page)
}