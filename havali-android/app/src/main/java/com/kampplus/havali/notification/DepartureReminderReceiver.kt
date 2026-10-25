package com.kampplus.havali.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kampplus.havali.R

/**
 * Android'in dört temel bileşeninden biri: BroadcastReceiver.
 *
 * AlarmManager hatırlatma zamanı gelince bu sınıfa bir yayın (broadcast) gönderir;
 * uygulama kapalı olsa bile sistem onu uyandırır. Receiver yalnızca bildirimi gösterir
 * ve hemen biter (onReceive kısa sürmelidir). Manifest'te `exported=false` olduğu için
 * başka uygulamalar bu yayını tetikleyemez.
 */
class DepartureReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val planId = intent.getStringExtra(EXTRA_PLAN_ID).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val place = intent.getStringExtra(EXTRA_PLACE).orEmpty()
        val leaveAt = intent.getStringExtra(EXTRA_LEAVE_AT).orEmpty()
        Log.d(TAG, "Hatırlatma geldi: $planId $leaveAt")
        NotificationHelper.showDeparture(
            context = context,
            notificationId = planId.hashCode(),
            title = context.getString(R.string.notification_departure_title, leaveAt),
            message = context.getString(R.string.notification_departure_message, title, place)
        )
    }

    companion object {
        private const val TAG = "DepartureReminder"
        const val EXTRA_PLAN_ID = "plan_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_PLACE = "place"
        const val EXTRA_LEAVE_AT = "leave_at"
    }
}
