package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.model.CheckupHistorySuccessModel
import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo


class CheckupHistoryUseCase(private val checkupRepo: CheckupRepo) {
    suspend fun execute(page: Int): CheckupHistorySuccessModel =
        checkupRepo.checkupHistory(page)
}