package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo

class GetSdkTokenUseCase(
    private val checkupSdkRepo: CheckupSdkRepo
) {
    suspend fun execute(appId: String, packageName: String) =
        checkupSdkRepo.getSDKToken(appId, packageName)
}