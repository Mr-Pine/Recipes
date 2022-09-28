package de.mr_pine.recipes.common.models.instructions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

fun InstructionSubmodels.TimerModel.call(title: String, context: Context) {
    val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
        putExtra(AlarmClock.EXTRA_MESSAGE, title)
        putExtra(AlarmClock.EXTRA_LENGTH, duration.inWholeSeconds.toInt())
        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
    }
    context.startActivity(intent)
}

