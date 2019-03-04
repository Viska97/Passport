package org.visapps.passport

import android.app.Application

class PassportApp : Application() {

    companion object {
        lateinit var instance: PassportApp
            private set
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

}