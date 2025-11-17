package com.example.control_tiempo_infantes.di

import com.example.control_tiempo_infantes.data.repository.*
import com.example.control_tiempo_infantes.data.user.*
import com.example.control_tiempo_infantes.data.child.*
import com.example.control_tiempo_infantes.domain.repository.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBinds {
    @Binds @Singleton abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindCircleRepository(impl: CircleRepositoryImpl): CircleRepository
    @Binds @Singleton abstract fun bindChildRepository(impl: ChildRepositoryImpl): ChildRepository
    @Binds
    @Singleton
    abstract fun bindInvitationRepository(
        impl: InvitationRepositoryImpl
    ): InvitationRepository

}

