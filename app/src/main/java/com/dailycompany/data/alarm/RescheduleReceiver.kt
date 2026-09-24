package com.dailycompany.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.repository.AlarmRepository
import com.dailycompany.domain.model.nextTrigger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Reagenda todos os alarmes ativos depois de acontecimentos que limpam o
 * `AlarmManager`: arranque do sistema (boot), mudança de hora/relógio, mudança
 * de fuso horário e atualização da app.
 *
 * Alvo da Fase 3: "Alarme toca; sobrevive a reinício".
 */
@AndroidEntryPoint
class RescheduleReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var scheduler: AlarmScheduler
    @Inject lateinit var clock: AppClock
    @Inject lateinit var ioDispatcher: CoroutineDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        // Injeção Hilt inserida pelo plugin no início (bytecode transform) —
        // em Kotlin não se chama super.onReceive() (super direto é abstrato).
        if (intent.action !in RESCHEDULE_ACTIONS) return

        val pending = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                val now = clock.now()
                repository.getEnabled().forEach { alarm ->
                    scheduler.schedule(alarm, alarm.nextTrigger(now))
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}