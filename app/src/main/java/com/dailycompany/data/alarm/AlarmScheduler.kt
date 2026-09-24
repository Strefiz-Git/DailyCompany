package com.dailycompany.data.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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

    /**
     * Agenda o toque principal de [alarm] para [at]. Devolve `true` se ficou
     * agendado. Nunca lança: se a permissão de alarmes exatos estiver revogada
     * (raro: `USE_EXACT_ALARM` é auto-concedida na instalação), devolve `false`
     * — o ecrã de ajuda explica como recuperar. Um crash silencioso aqui
     * matava a app no meio do `save()` (visto no emulador, API 37).
     */
    @SuppressLint("MissingPermission") // setAlarmClock exige USE_EXACT_ALARM (declarada no manifest; Android 14+).
    fun schedule(alarm: Alarm, at: LocalDateTime): Boolean {
        if (!canScheduleExactAlarms()) return false
        return runCatching {
            val info = AlarmManager.AlarmClockInfo(at.atZone(clock.zone()).toInstant().toEpochMilli(), null)
            alarmManager.setAlarmClock(info, alarmPendingIntent(alarm.id))
        }.isSuccess
    }

    /** Cancela o toque principal de um alarme (não mexe num snooze já pendente). */
    fun cancel(alarmId: Long) {
        alarmManager.cancel(alarmPendingIntent(alarmId))
    }

    /** Agenda um toque extra único (snooze manual ou automático). */
    @SuppressLint("MissingPermission") // idem: setAlarmClock exige USE_EXACT_ALARM (declarada no manifest).
    fun scheduleSnooze(alarmId: Long, at: LocalDateTime): Boolean {
        if (!canScheduleExactAlarms()) return false
        return runCatching {
            val info = AlarmManager.AlarmClockInfo(at.atZone(clock.zone()).toInstant().toEpochMilli(), null)
            alarmManager.setAlarmClock(info, snoozePendingIntent(alarmId))
        }.isSuccess
    }

    /** Com `USE_EXACT_ALARM` concedida, `canScheduleExactAlarms()` é sempre `true` (API 31+). */
    private fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

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