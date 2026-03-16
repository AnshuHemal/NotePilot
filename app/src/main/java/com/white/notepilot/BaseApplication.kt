package com.white.notepilot

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.white.notepilot.utils.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        NotificationChannelManager.createNotificationChannels(this)
    }
}