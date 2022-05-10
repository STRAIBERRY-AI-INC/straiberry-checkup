package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo

class GetSdkTokenUseCase(
    private val checkupRepo: CheckupRepo
) {
    suspend fun execute(appId: String, packageName: String) =
        checkupRepo.getSDKToken(appId, packageName)
}