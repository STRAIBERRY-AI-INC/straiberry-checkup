package com.straiberry.android.checkup.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.koinApplication

// Modules
val appModules = listOf(presentationModule, libModule)
val dataModules = listOf(networkingModule, repositoryModule)
val domainModules = listOf(interactionModule)

// Custom Koin instance Holder
object StraiberrySdk {
    internal var app: KoinApplication? = null

    @JvmStatic
    fun start(applicationContext: Context) {
        app = buildKoinApplication(applicationContext)
    }

    @JvmStatic
    fun stop() = synchronized(this) {
        app?.close()
        app = null
    }

    @JvmStatic
    fun get(): KoinApplication = app
        ?: error("KoinApplication for CustomSDK has not been started")

    private fun buildKoinApplication(applicationContext: Context): KoinApplication {
        return koinApplication {
            androidContext(applicationContext)
            modules(appModules + dataModules + domainModules)
        }
    }

    fun setup(mContext: Context) {
        GlobalContext.getOrNull() ?: startKoin {
            androidContext(mContext)
            modules(
                appModules + dataModules + domainModules
            )
        }
    }
}

// Custom KoinComponent
interface IsolatedKoinComponent : KoinComponent {
    override fun getKoin(): Koin = StraiberrySdk.get().koin
}