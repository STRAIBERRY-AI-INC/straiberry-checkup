package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupDataStore
import com.straiberry.android.common.model.GuideTourStatusModel


class SetGuideStatusUseCase(
    private val checkupDataStore: CheckupDataStore
) {
    fun execute(guideTourStatusModel: GuideTourStatusModel) =
        checkupDataStore.setGuideTourStatus(guideTourStatusModel)
}