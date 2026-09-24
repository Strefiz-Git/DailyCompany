package com.dailycompany.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dailycompany.MainActivity
import com.dailycompany.domain.model.Alarm
import com.dailycompany.ui.theme.AppStrings
import java.time.format.DateTimeFormatter

/**
 * Criação do canal e das notificações de alarmes. O canal é criado no momento
 * com as strings da língua atual ([AppStrings]) — regra 4 do AGENTS: nenhum
 * texto visível escrito à mão, mesmo fora do Compose.
 *
 * O canal existe desde a API 26 (minSdk) — sem ramos de compatibilidade.
 */
object AlarmNotifier {

    const val CHANNEL_ALARMS_ID = "alarms"

    /** Snooze manual fixo — a etiqueta da ação ("Adiar 10 min") definiu o valor. */
    const val SNOOZE_MINUTES = 10

    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /** Mostra (ou volta a mostrar, [snoozed]) a notificação de [alarm]. */
    fun showAlarmNotification(
        context: Context,
        alarm: Alarm,
        strings: AppStrings,
        snoozed: Boolean,
    ) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel(context, strings))

        val builder = NotificationCompat.Builder(context, CHANNEL_ALARMS_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (alarm.label.isBlank()) strings.alarmsTitle else alarm.label)
            .setContentText(alarm.time.format(timeFormat))
            .setContentIntent(openApp(context, alarm.id))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (!alarm.soundEnabled) builder.setSound(null)
        if (alarm.vibrateEnabled) builder.setVibrate(longArrayOf(0, 400, 400, 400))

        if (!snoozed) {
            builder.addAction(0, strings.notificationActionSnooze, snoozeNowIntent(context, alarm.id))
        }
        builder.addAction(0, strings.notificationActionDone, doneIntent(context, alarm.id))

        manager.notify(alarm.id.toInt(), builder.build())
    }

    fun dismiss(context: Context, alarmId: Long) {
        context.getSystemService(NotificationManager::class.java).cancel(alarmId.toInt())
    }

    private fun channel(context: Context, strings: AppStrings): NotificationChannel =
        NotificationChannel(
            CHANNEL_ALARMS_ID,
            strings.notificationChannelAlarms,
            NotificationManager.IMPORTANCE_HIGH,
        )

    private fun openApp(context: Context, alarmId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            alarmId.toInt() + 100,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun snoozeNowIntent(context: Context, alarmId: Long): PendingIntent =
        broadcastAction(context, alarmId, requestCodeOffset = 200, action = AlarmReceiver.ACTION_SNOOZE_NOW)

    private fun doneIntent(context: Context, alarmId: Long): PendingIntent =
        broadcastAction(context, alarmId, requestCodeOffset = 300, action = AlarmReceiver.ACTION_DONE)

    private fun broadcastAction(
        context: Context,
        alarmId: Long,
        requestCodeOffset: Int,
        action: String,
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId.toInt() + requestCodeOffset,
        Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}