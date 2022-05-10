package com.straiberry.android.checkup.checkup.domain.usecase

import com.straiberry.android.checkup.checkup.domain.repository.CheckupDataStore


class CheckupHelpHasBeenSeenUseCase(private val checkupDataStore: CheckupDataStore) {
    fun execute() = checkupDataStore.checkupHelpHasBeenSeen()
}