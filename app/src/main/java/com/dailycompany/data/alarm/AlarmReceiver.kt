package com.dailycompany.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.repository.AlarmRepository
import com.dailycompany.data.settings.StringsProvider
import com.dailycompany.domain.model.AlarmPostFire
import com.dailycompany.domain.model.postFireAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Recebe os toques agendados ([ACTION_ALARM] e snooze) e as ações das
 * notificações ([ACTION_SNOOZE_NOW], [ACTION_DONE]).
 *
 * Injeção Hilt: a base gerada injeta no `super.onReceive(context, intent)` —
 * chamada obrigatória antes de usar os campos (documentação Hilt).
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var scheduler: AlarmScheduler
    @Inject lateinit var clock: AppClock
    @Inject lateinit var stringsProvider: StringsProvider
    @Inject lateinit var ioDispatcher: CoroutineDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        // A injeção Hilt é inserida pelo plugin no início deste método
        // (bytecode transform, marcador @OnReceiveBytecodeInjectionMarker) —
        // em Kotlin não se chama super.onReceive() neste caso.
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId < 0L) return

        when (intent.action) {
            ACTION_ALARM -> fire(context, alarmId)
            ACTION_SNOOZE -> showSnoozed(context, alarmId)
            ACTION_SNOOZE_NOW -> snoozeNow(context, alarmId)
            ACTION_DONE -> AlarmNotifier.dismiss(context, alarmId)
        }
    }

    /** O alarme tocou: notifica e aplica a decisão pura (reagendar / desligar + auto-snooze). */
    private fun fire(context: Context, alarmId: Long) {
        val pending = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                val alarm = repository.getById(alarmId) ?: return@launch
                if (!alarm.enabled) return@launch

                AlarmNotifier.showAlarmNotification(
                    context,
                    alarm,
                    stringsProvider.current(),
                    snoozed = false,
                )

                val action = alarm.postFireAction(clock.now())
                when (action) {
                    is AlarmPostFire.Reschedule -> scheduler.schedule(alarm, action.next)
                    is AlarmPostFire.Disable -> repository.setEnabled(alarmId, false)
                }
                action.autoSnoozeAt?.let { scheduler.scheduleSnooze(alarmId, it) }
            } finally {
                pending.finish()
            }
        }
    }

    /** Um snooze (manual ou automático) voltou a tocar: mostra a notificação de novo. */
    private fun showSnoozed(context: Context, alarmId: Long) {
        val pending = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                val alarm = repository.getById(alarmId) ?: return@launch
                AlarmNotifier.showAlarmNotification(context, alarm, stringsProvider.current(), snoozed = true)
            } finally {
                pending.finish()
            }
        }
    }

    /** Ação "Adiar 10 min": retira a notificação atual e agenda o toque do snooze. */
    private fun snoozeNow(context: Context, alarmId: Long) {
        AlarmNotifier.dismiss(context, alarmId)
        val pending = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                scheduler.scheduleSnooze(alarmId, clock.now().plusMinutes(AlarmNotifier.SNOOZE_MINUTES.toLong()))
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_ALARM = "com.dailycompany.action.ALARM"
        const val ACTION_SNOOZE = "com.dailycompany.action.SNOOZE"
        const val ACTION_SNOOZE_NOW = "com.dailycompany.action.SNOOZE_NOW"
        const val ACTION_DONE = "com.dailycompany.action.DONE"
        const val EXTRA_ALARM_ID = "com.dailycompany.extra.ALARM_ID"
    }
}