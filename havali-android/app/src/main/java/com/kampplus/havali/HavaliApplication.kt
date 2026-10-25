package com.kampplus.havali

import android.app.Application
import android.util.Log
import com.kampplus.havali.di.AppContainer
import com.kampplus.havali.notification.NotificationHelper

/**
 * Uygulama süreci başlarken, herhangi bir Activity'den önce bir kez oluşturulur.
 * Bağımlılık kabı ve bildirim kanalı burada kurulur. Manifest'te `android:name` ile bağlıdır.
 */
class HavaliApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannels(this)
        Log.d(TAG, "Uygulama başladı")
    }

    private companion object {
        const val TAG = "Lifecycle"
    }
}
