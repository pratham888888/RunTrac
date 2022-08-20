package com.example.runtrac

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
//used this to tell our app that we want to inject dependencies using dagger hilt
class BaseApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        // used for logging
        Timber.plant(Timber.DebugTree())
    }

}