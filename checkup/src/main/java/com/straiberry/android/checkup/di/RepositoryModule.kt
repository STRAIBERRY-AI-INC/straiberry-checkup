package com.straiberry.android.checkup.di

import com.straiberry.android.checkup.checkup.data.repository.FlowCheckupDataStore
import com.straiberry.android.checkup.checkup.data.repository.RemoteCheckupRepo
import com.straiberry.android.checkup.checkup.data.repository.RemoteCheckupSdkRepo
import com.straiberry.android.checkup.checkup.data.repository.RemoteXrayRepo
import com.straiberry.android.checkup.checkup.domain.repository.CheckupDataStore
import com.straiberry.android.checkup.checkup.domain.repository.CheckupRepo
import com.straiberry.android.checkup.checkup.domain.repository.CheckupSdkRepo
import com.straiberry.android.checkup.checkup.domain.repository.XRayRepo
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<CheckupRepo> { RemoteCheckupRepo(get(), get(), get()) }
    single<CheckupSdkRepo> { RemoteCheckupSdkRepo(get(), get(), get()) }
    single<XRayRepo> { RemoteXrayRepo(get(), get(), get()) }
    single<CheckupDataStore> { FlowCheckupDataStore(androidContext()) }
}