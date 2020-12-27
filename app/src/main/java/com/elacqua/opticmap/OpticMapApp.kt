package com.elacqua.opticmap

import android.app.Application
import timber.log.Timber

class OpticMapApp: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }
}