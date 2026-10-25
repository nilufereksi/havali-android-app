package com.kampplus.havali.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kampplus.havali.domain.model.DepartureReminder
import com.kampplus.havali.domain.repository.ReminderScheduler
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * [ReminderScheduler]'ın Android uygulaması.
 *
 * `AlarmManager.set` tam saniyesinde olmayabilir (sistem pil için birkaç dakika
 * kaydırabilir) ama özel izin istemez; çıkış hatırlatması için bu yeterlidir.
 * Hatırlatma zamanı geçmişse (demo sırasında sık olur) birkaç saniye sonra düşer.
 */
class AlarmReminderScheduler(
    private val context: Context,
    private val clock: () -> LocalDateTime = { LocalDateTime.now() }
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(reminder: DepartureReminder) {
        val earliest = clock().plusSeconds(DEMO_DELAY_SECONDS)
        val triggerAt = reminder.remindAt.takeIf { it.isAfter(earliest) } ?: earliest
        val epochMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.set(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent(reminder))
    }

    override fun cancel(planId: String) {
        alarmManager.cancel(pendingIntent(DepartureReminder(planId, "", "", clock(), clock().toLocalTime())))
    }

    /** Aynı plan için hep aynı requestCode: yeniden kurmak eskisinin yerine geçer, iptal de onu bulur. */
    private fun pendingIntent(reminder: DepartureReminder): PendingIntent {
        val intent = Intent(context, DepartureReminderReceiver::class.java)
            .putExtra(DepartureReminderReceiver.EXTRA_PLAN_ID, reminder.planId)
            .putExtra(DepartureReminderReceiver.EXTRA_TITLE, reminder.planTitle)
            .putExtra(DepartureReminderReceiver.EXTRA_PLACE, reminder.placeName)
            .putExtra(DepartureReminderReceiver.EXTRA_LEAVE_AT, reminder.leaveAt.format(TIME_FORMAT))
        return PendingIntent.getBroadcast(
            context,
            reminder.planId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private companion object {
        const val DEMO_DELAY_SECONDS = 5L
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
