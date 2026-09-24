package com.dailycompany.data.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dailycompany.core.time.AppClock
import com.dailycompany.domain.model.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Único ponto que mexe no `AlarmManager` (regra AGENTS: nenhum outro ficheiro).
 *
 * Os alarmes usam [AlarmManager.setAlarmClock] — o mecanismo mais fiável do
 * Android para alarmes de relógio: toca mesmo em Doze, mostra o ícone de
 * alarme (hora do próximo toque) e NÃO depende da permissão de alarmes exatos.
 *
 * Só a **próxima** ocorrência é agendada (nada de `setRepeating`, inexato
 * desde a API 19); o próprio `AlarmReceiver` reagenda a seguinte quando toca.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: AppClock,
) {
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    /** Agenda o toque principal de [alarm] para [at] (converte para millis no fuso atual). */
    @SuppressLint("MissingPermission") // setAlarmClock está isento de SCHEDULE_EXACT_ALARM (uso de relógio, Android 12+).
    fun schedule(alarm: Alarm, at: LocalDateTime) {
        val info = AlarmManager.AlarmClockInfo(at.atZone(clock.zone()).toInstant().toEpochMilli(), null)
        alarmManager.setAlarmClock(info, alarmPendingIntent(alarm.id))
    }

    /** Cancela o toque principal de um alarme (não mexe num snooze já pendente). */
    fun cancel(alarmId: Long) {
        alarmManager.cancel(alarmPendingIntent(alarmId))
    }

    /** Agenda um toque extra único (snooze manual ou automático). */
    @SuppressLint("MissingPermission") // idem: isenção de setAlarmClock para alarmes de relógio.
    fun scheduleSnooze(alarmId: Long, at: LocalDateTime) {
        val info = AlarmManager.AlarmClockInfo(at.atZone(clock.zone()).toInstant().toEpochMilli(), null)
        alarmManager.setAlarmClock(info, snoozePendingIntent(alarmId))
    }

    private fun alarmPendingIntent(alarmId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId.toInt(),
        Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun snoozePendingIntent(alarmId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId.toInt(),
        Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}