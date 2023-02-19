package com.straiberry.android.checkup.di

import com.straiberry.android.checkup.checkup.domain.usecase.*
import org.koin.dsl.module

val interactionModule = module {
    single { CreateCheckupUseCase(get()) }
    single { AddImageToCheckupUseCase(get()) }
    single { AddToothToCheckupUseCase(get()) }
    single { DeleteCheckupUseCase(get()) }
    single { DeleteToothInCheckupUseCase(get()) }
    single { UpdateImageInCheckupUseCase(get()) }
    single { UpdateToothInCheckupUseCase(get()) }
    single { AddSeveralTeethToCheckupUseCase(get()) }
    single { CheckupHistoryUseCase(get()) }
    single { AddDentalIssueUseCase(get()) }
    single { GetAllDentalIssueUseCase(get()) }
    single { DeleteDentalIssueUseCase(get()) }
    single { RemoteAddDentalIssueUseCase(get()) }
    single { RemoteDeleteDentalIssueUseCase(get()) }
    single { CheckupHelpHasBeenSeenUseCase(get()) }
    single { ShouldShowCheckupHelpUseCase(get()) }
    single { GetGuideTourStatusUseCase(get()) }
    single { SetGuideStatusUseCase(get()) }
    single { GetSdkTokenUseCase(get()) }
    single { GetCheckupResultUseCase(get()) }
    single { RemoteUpdateDentalIssueUseCase(get()) }
    single { CreateXrayCheckupUseCase(get()) }
    single { AddXrayImageFromUrlUseCase(get()) }
    single { CheckXrayUrlUseCase(get()) }
    single { CreateCheckupSdkUseCase(get()) }
    single { GetCheckupSdkResultUseCase(get()) }
    single { AddImageToCheckupSdkUseCase(get()) }
    single { UpdateImageInCheckupSdkUseCase(get()) }
    single { CheckupSdkHistoryUseCase(get()) }
    single { AddImageToXrayCheckupUseCase(get()) }
}
