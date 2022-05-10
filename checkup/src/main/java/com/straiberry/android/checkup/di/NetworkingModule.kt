package com.straiberry.android.checkup.di

import com.straiberry.android.checkup.checkup.data.networking.CheckupApi
import com.straiberry.android.checkup.checkup.data.networking.XRayApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api.dev.straiberry.com/"
const val DEBUG = "DEBUG"
val networkingModule = module {
    single { GsonConverterFactory.create() as Converter.Factory }
    single {
        OkHttpClient.Builder().apply {
//            if (androidContext().getBuildConfigValue(DEBUG) as Boolean)
            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))

            connectTimeout(1, TimeUnit.MINUTES)
            readTimeout(45, TimeUnit.SECONDS)
            writeTimeout(15, TimeUnit.SECONDS)
            retryOnConnectionFailure(false)
//            addInterceptor(HttpErrorInterceptor())
        }.build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(get())
            .build()
    }

    single<Retrofit>(named("x-ray")) {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(get())
            .build()
    }

    single { get<Retrofit>().create(CheckupApi::class.java) }
    single { get<Retrofit>(named("x-ray")).create(XRayApi::class.java) }
}