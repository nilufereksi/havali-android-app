package com.kampplus.havali.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kampplus.havali.MainActivity
import com.kampplus.havali.R

/**
 * Bildirim kanalı ve bildirim oluşturma tek yerde.
 *
 * Android 8.0+ (API 26) her bildirimin bir kanala ait olmasını ister; kanal uygulama
 * açılışında [HavaliApplication][com.kampplus.havali.HavaliApplication] içinde kurulur.
 * Android 13+ (API 33) için ayrıca POST_NOTIFICATIONS izni gerekir; izin yoksa bildirim
 * sessizce atlanır (uygulama çökmez).
 */
object NotificationHelper {

    const val DEPARTURE_CHANNEL_ID = "departure"

    fun createChannels(context: Context) {
        val channel = NotificationChannel(
            DEPARTURE_CHANNEL_ID,
            context.getString(R.string.notification_channel_departure),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = context.getString(R.string.notification_channel_departure_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun showDeparture(context: Context, notificationId: Int, title: String, message: String) {
        val openApp = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, DEPARTURE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        notifyIfAllowed(context, notificationId, notification)
    }

    @SuppressLint("MissingPermission") // İzin canNotify() ile kontrol ediliyor.
    private fun notifyIfAllowed(context: Context, id: Int, notification: Notification) {
        context.takeIf(::canNotify)?.let { NotificationManagerCompat.from(it).notify(id, notification) }
    }
}
