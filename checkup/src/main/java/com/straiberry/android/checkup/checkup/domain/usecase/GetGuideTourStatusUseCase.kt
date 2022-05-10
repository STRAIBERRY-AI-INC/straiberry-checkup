package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupDataStore

class GetGuideTourStatusUseCase(
    private val checkupDataStore: CheckupDataStore
) {
    fun execute() = checkupDataStore.getGuideTourStatus()
}