package com.alpharays.autosound.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.alpharays.autosound.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random


class AutoSoundTriggerReceiver : BroadcastReceiver() {
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private val channelId = "com.alpharays.autosound"

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onCheckedChanged: Receiver start")

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val ringerMode =
            Constants.RingerMode.valueOf(intent.getStringExtra(Constants.RINGER_MODE_BUNDLE_NAME)!!)
        val volumes = intent.getIntArrayExtra(Constants.VOLUMES_BUNDLE_NAME)?.toList()

        val timeInfo = intent.getStringExtra("timeInfo")

        Log.i("checkingAlarmTimeInfo", timeInfo.toString())
//        Toast.makeText(context, timeInfo.toString(), Toast.LENGTH_LONG).show()


        volumes?.let {
            val maxRingVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
            val maxNotificationVolume =
                audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            val maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

            val ringerVolume = Utilities.percentOf(maxRingVolume, volumes[0])
            val notificationVolume = Utilities.percentOf(maxNotificationVolume, volumes[0])
            val mediaVolume = Utilities.percentOf(maxMediaVolume, volumes[1])
            val alarmVolume = Utilities.percentOf(maxAlarmVolume, volumes[2])


            when (ringerMode) {
                Constants.RingerMode.DND -> {
                    // Set the ringer mode to DND
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT

                    // Set the system volume to zero
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)

                    // Set the media volume and alarm volume to the desired levels
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        mediaVolume,
                        AudioManager.FLAG_VIBRATE
                    )
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_ALARM,
                        alarmVolume,
                        AudioManager.FLAG_VIBRATE
                    )
                }

                Constants.RingerMode.Normal -> {
                    // Set the ringer mode to Normal
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_RING,
                        ringerVolume,
                        AudioManager.FLAG_VIBRATE
                    )
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_NOTIFICATION,
                        notificationVolume,
                        AudioManager.FLAG_VIBRATE
                    )
                }

                Constants.RingerMode.Vibrate -> {
                    // Set the ringer mode to Vibrate
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }

                Constants.RingerMode.Silent -> {
                    // Set the ringer mode to Silent
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
            }

            // Set the media volume and alarm volume to the desired levels (common for all modes)
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mediaVolume,
                AudioManager.FLAG_VIBRATE
            )
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                alarmVolume,
                AudioManager.FLAG_VIBRATE
            )


            createNotificationChannel(notificationManager)
            // Cancel all previous notifications
            notificationManager.cancelAll()

            val drawable: Drawable? =
                ResourcesCompat.getDrawable(context.resources, R.drawable.app_icon, null)
            val largeIconBitmap: Bitmap? = drawable?.toBitmap()

            var notificationId = 0
            if (largeIconBitmap != null) {
                val notification = NotificationCompat.Builder(context, channelId)
                    .setLargeIcon(largeIconBitmap)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle("AutoSound Triggered")
                    .setContentText("Ringer mode set to $ringerMode at $timeInfo")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setAutoCancel(true)
                    .build()
                notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
                notificationId = Random.nextInt()
                notificationManager.notify(notificationId, notification)
            } else {
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle("AutoSound Triggered")
                    .setContentText("Ringer mode set to ${ringerMode.toString()}")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setAutoCancel(true)
                    .build()
                notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
                notificationId = Random.nextInt()
                notificationManager.notify(notificationId, notification)
            }

            val handler = Handler(Looper.getMainLooper())
            val delayMillis = 5000L // Delay in milliseconds (e.g., 5000L for 5 seconds)

            handler.postDelayed({
                notificationManager.cancel(notificationId) // Remove the notification after the delay
            }, delayMillis)

            Log.d(TAG, "onCheckedChanged: Receiver end")
        }
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        val channel = NotificationChannel(
            channelId,
            "ShivangAutoSound",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "New trigger"
        channel.enableLights(true)
        manager.createNotificationChannel(channel)
    }
}
