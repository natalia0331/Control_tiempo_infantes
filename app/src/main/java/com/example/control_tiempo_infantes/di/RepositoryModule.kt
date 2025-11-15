package com.example.control_tiempo_infantes.di

import com.example.control_tiempo_infantes.data.auth.AuthRepositoryImpl
import com.example.control_tiempo_infantes.data.user.UserRepositoryImpl
import com.example.control_tiempo_infantes.data.circles.CircleRepositoryImpl
import com.example.control_tiempo_infantes.domain.repository.AuthRepository
import com.example.control_tiempo_infantes.domain.repository.UserRepository
import com.example.control_tiempo_infantes.domain.repository.CircleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindCircleRepository(impl: CircleRepositoryImpl): CircleRepository
}
