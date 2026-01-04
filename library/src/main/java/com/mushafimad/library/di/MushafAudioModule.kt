package com.mushafimad.library.di

import com.mushafimad.library.data.audio.AudioPlayerService
import com.mushafimad.library.data.audio.AyahTimingService
import com.mushafimad.library.data.repository.AudioRepositoryImpl
import com.mushafimad.library.domain.repository.AudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing audio playback dependencies
 * Includes Media3 ExoPlayer, audio session management, and recitation services
 */
@Module
@InstallIn(SingletonComponent::class)
object MushafAudioModule {

    /**
     * Provides AudioRepository for audio playback and reciter management
     */
    @Provides
    @Singleton
    internal fun provideAudioRepository(
        audioPlayerService: AudioPlayerService,
        ayahTimingService: AyahTimingService
    ): AudioRepository = AudioRepositoryImpl(audioPlayerService, ayahTimingService)
}
