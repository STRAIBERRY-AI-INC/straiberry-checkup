package com.straiberry.android.checkup.di

import com.straiberry.android.checkup.checkup.presentation.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { CheckupQuestionViewModel() }
    viewModel { CheckupResultProblemIllustrationViewModel() }
    viewModel { CheckupResultLastSelectedProblemViewModel() }
    viewModel { DetectionJawViewModel() }
    viewModel { CheckupQuestionSubmitTeethViewModel(get(), get()) }
    viewModel { CheckupSubmitImageViewModel(get(), get(), get()) }
    viewModel { CheckupHistoryViewModel(get(), get()) }
    viewModel { CreateCheckupViewModel(get(), get(), get()) }
    viewModel { DentalIssuesViewModel(get(), get(), get(), get()) }
    viewModel { RemoteDentalIssueViewModel(get(), get(), get(), get()) }
    viewModel { CheckupGuideTourViewModel(get(), get()) }
    viewModel { CheckupHelpViewModel(get(), get()) }
    viewModel { CheckupResultViewModel(get(), get()) }
    viewModel { XrayViewModel(get(), get(), get(), get(), get()) }
}