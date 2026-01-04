package com.mushafimad.library.di

import android.content.Context
import com.mushafimad.library.data.repository.PreferencesRepositoryImpl
import com.mushafimad.library.domain.repository.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing user preferences and settings dependencies
 * Includes DataStore preferences, theme settings, and user configurations
 */
@Module
@InstallIn(SingletonComponent::class)
object MushafPreferencesModule {

    @Provides
    @Singleton
    internal fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(context)
    }
}
