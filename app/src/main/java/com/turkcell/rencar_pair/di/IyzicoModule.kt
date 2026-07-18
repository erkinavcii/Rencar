package com.turkcell.rencar_pair.di

import com.turkcell.rencar_pair.data.repository.IyzicoRepository
import com.turkcell.rencar_pair.data.repository.IyzicoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IyzicoModule {
    @Binds
    @Singleton
    abstract fun bindIyzicoRepository(impl: IyzicoRepositoryImpl): IyzicoRepository
}
