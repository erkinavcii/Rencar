package com.turkcell.rencar_pair.di

import com.turkcell.rencar_pair.data.remote.AdminApprovalService
import com.turkcell.rencar_pair.data.remote.ApiConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * "AI ile Anında Onayla" demo akisi icin kasitli olarak izole ag katmani.
 *
 * NetworkModule'un OkHttpClient'i AuthInterceptor tasir; bu interceptor musteri
 * oturumu acikken HER istege TokenManager'daki musteri access token'ini kosulsuz
 * basar (bkz. AuthInterceptor.intercept). Admin onayi cagrisinin ADMIN token'i
 * tasimasi gerektiginden, o client uzerinden gidilirse musteri token'i admin
 * token'in yerini alir ve cagri 403 doner. Bu yuzden burada AuthInterceptor/
 * TokenAuthenticator icermeyen, tamamen ayri bir OkHttpClient+Retrofit kuruluyor;
 * musteri oturumuna (TokenManager, SessionManager) hic dokunmaz.
 *
 * Json ve loglama Interceptor'i NetworkModule'daki mevcut @Singleton binding'lerden
 * enjekte edilir (tekrar tanimlanmiyor).
 */
@Module
@InstallIn(SingletonComponent::class)
object AdminApprovalModule {

    @Provides
    @Singleton
    fun provideAdminApprovalService(
        json: Json,
        loggingInterceptor: Interceptor,
    ): AdminApprovalService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return retrofit.create(AdminApprovalService::class.java)
    }
}
