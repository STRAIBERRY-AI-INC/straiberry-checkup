package com.straiberry.android.checkup.di

import com.straiberry.android.checkup.common.helper.SdkAuthorizationHelper
import com.straiberry.android.common.network.CoroutineContextProvider
import org.koin.dsl.module

val libModule = module {
    single { CoroutineContextProvider() }
    single { SdkAuthorizationHelper }
}