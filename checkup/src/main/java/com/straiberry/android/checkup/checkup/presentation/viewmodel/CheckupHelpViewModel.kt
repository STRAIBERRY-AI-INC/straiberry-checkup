package com.straiberry.android.checkup.checkup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.straiberry.android.checkup.checkup.domain.usecase.CheckupHelpHasBeenSeenUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.ShouldShowCheckupHelpUseCase

class CheckupHelpViewModel(
    private val shouldShowCheckupHelpUseCase: ShouldShowCheckupHelpUseCase,
    private val checkupHelpHasBeenSeenUseCase: CheckupHelpHasBeenSeenUseCase
) : ViewModel() {
    fun shouldShowCheckupHelp() = shouldShowCheckupHelpUseCase.execute()

    fun checkupHelpHasBeenSeen() = checkupHelpHasBeenSeenUseCase.execute()
}