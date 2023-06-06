package com.alpharays.autosound.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alpharays.autosound.data.trigger.Trigger
import java.util.Calendar

class AlarmManagerHandler(
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val calendar: Calendar
) {
    fun setAlarm(requestCode: Int, trigger: Trigger) {
        val intent = Intent(context, AutoSoundTriggerReceiver::class.java)
        intent.putExtra(Constants.RINGER_MODE_BUNDLE_NAME, trigger.ringerMode)
        val arrayList = arrayListOf(trigger.ringerVolume, trigger.mediaVolume, trigger.alarmVolume)
        intent.putExtra(Constants.VOLUMES_BUNDLE_NAME, arrayList)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        if (trigger.isRepeat) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, AutoSoundTriggerReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        alarmManager.cancel(pendingIntent)
    }
}