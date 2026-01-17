package com.mushafimad.sampleapp

import android.app.Application
import com.mushafimad.core.MushafLibrary

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Optional: custom logger/analytics
        // MushafLibrary.setLogger(CustomLogger())
        // MushafLibrary.setAnalytics(CustomAnalytics())
    }
}
