package com.turkcell.rencar_pair.di

import com.turkcell.rencar_pair.data.remote.ReservationService
import com.turkcell.rencar_pair.data.repository.ReservationRepository
import com.turkcell.rencar_pair.data.repository.ReservationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReservationModule {

    @Binds
    @Singleton
    abstract fun bindReservationRepository(
        impl: ReservationRepositoryImpl
    ): ReservationRepository

    companion object {
        @Provides
        @Singleton
        fun provideReservationService(retrofit: Retrofit): ReservationService =
            retrofit.create(ReservationService::class.java)
    }
}
