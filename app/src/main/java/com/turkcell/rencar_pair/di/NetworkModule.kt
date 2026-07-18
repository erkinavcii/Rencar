package com.turkcell.rencar_pair.di

import android.content.Context
import com.turkcell.rencar_pair.BuildConfig
import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.data.remote.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): Interceptor {
        // Release: hicbir sey loglanmaz. Govde loglari token/kisisel veri (ehliyet,
        // telefon, kart) sizdirir; Authorization header'i da acikca gorunur.
        if (!BuildConfig.DEBUG) {
            return Interceptor { chain -> chain.proceed(chain.request()) }
        }

        // Debug: normal (JSON) istekler tam govdeyle loglanir - API hata ayiklamasi
        // icin gerekli. Ancak multipart istekler (foto yukleme) yalniz header'la
        // loglanir: binary JPEG govdesi Logcat'i okunamaz cop karakterlerle
        // dolduruyordu. HttpLoggingInterceptor bunu kendisi ayiklayamiyor cunku
        // isProbablyUtf8 yalniz govdenin basina (multipart sinir + alan adlari,
        // UTF-8) bakip tumunu metin saniyor.
        val fullBodyLogger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val headersOnlyLogger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        return Interceptor { chain ->
            val isMultipart = chain.request().body?.contentType()?.type
                .equals("multipart", ignoreCase = true)
            val delegate = if (isMultipart) headersOnlyLogger else fullBodyLogger
            delegate.intercept(chain)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: Interceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideLicenseService(retrofit: Retrofit): LicenseService =
        retrofit.create(LicenseService::class.java)

    @Provides
    @Singleton
    fun provideVehicleService(retrofit: Retrofit): VehicleService =
        retrofit.create(VehicleService::class.java)

    @Provides
    @Singleton
    fun provideRentalService(retrofit: Retrofit): RentalService =
        retrofit.create(RentalService::class.java)

    @Provides
    @Singleton
    fun provideWalletService(retrofit: Retrofit): WalletService =
        retrofit.create(WalletService::class.java)

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager =
        TokenManager(context)
}
