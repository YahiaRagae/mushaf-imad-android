package com.mushafimad.sampleapp

import android.app.Application
import com.mushafimad.library.MushafLibrary
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Mushaf library
        MushafLibrary.initialize(
            context = this
            // logger = FirebaseMushafLogger() // Optional: custom logger
            // analytics = FirebaseMushafAnalytics() // Optional: custom analytics
        )
    }
}
