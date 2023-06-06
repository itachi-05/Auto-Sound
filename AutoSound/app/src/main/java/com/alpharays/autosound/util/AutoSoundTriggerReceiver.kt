package com.alpharays.autosound.util

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alpharays.autosound.R


class AutoSoundTriggerReceiver : BroadcastReceiver() {
    private var audioManager: AudioManager? = null
    private var notificationManager: NotificationManager? = null
    private var ringerMode: Constants.RingerMode? = null
    private var ringerVolume = 0
    private var mediaVolume = 0
    private var alarmVolume = 0
    private val channelId = "com.alpharays.autosound"

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onCheckedChanged: Receiver start")
        ringerMode =
            Constants.RingerMode.valueOf(intent.getStringExtra(Constants.RINGER_MODE_BUNDLE_NAME)!!)
        val volumes = intent.getIntArrayExtra(Constants.VOLUMES_BUNDLE_NAME)

        volumes?.let {
            Log.d(TAG, "onReceive: Volumes : " + volumes[0] + ", " + volumes[1] + ", " + volumes[2])

            if (audioManager == null) {
                audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            }
            if (notificationManager == null) {
                notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            audioManager!!.ringerMode = ringerMode!!.value
            ringerVolume = Utilities.percentOf(
                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_RING),
                volumes[0]
            )
            val notificationVolume = Utilities.percentOf(
                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), volumes[0]
            )
            mediaVolume = Utilities.percentOf(
                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                volumes[1]
            )
            alarmVolume = Utilities.percentOf(
                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                volumes[2]
            )


            if (ringerMode == Constants.RingerMode.DND) {
                // Set the ringer mode to DND
                audioManager!!.ringerMode = AudioManager.RINGER_MODE_SILENT

                // Set the system volume to zero
                audioManager!!.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)

                // Set the media volume to the desired level
                mediaVolume = Utilities.percentOf(
                    audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    volumes[1]
                )
                audioManager!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    mediaVolume,
                    AudioManager.FLAG_VIBRATE
                )

                // Set the alarm volume to the desired level
                alarmVolume = Utilities.percentOf(
                    audioManager!!.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                    volumes[2]
                )
                audioManager!!.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    alarmVolume,
                    AudioManager.FLAG_VIBRATE
                )
            } else if (ringerMode == Constants.RingerMode.Normal) {
                // Set the ringer mode to Normal
                audioManager!!.ringerMode = AudioManager.RINGER_MODE_NORMAL
                audioManager!!.setStreamVolume(
                    AudioManager.STREAM_RING,
                    ringerVolume,
                    AudioManager.FLAG_VIBRATE
                )
                audioManager!!.setStreamVolume(
                    AudioManager.STREAM_NOTIFICATION,
                    notificationVolume,
                    AudioManager.FLAG_VIBRATE
                )
            } else if (ringerMode == Constants.RingerMode.Vibrate) {
                // Set the ringer mode to Vibrate
                audioManager!!.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            } else if (ringerMode == Constants.RingerMode.Silent) {
                // Set the ringer mode to Silent
                audioManager!!.ringerMode = AudioManager.RINGER_MODE_SILENT
            }

            audioManager!!.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mediaVolume,
                AudioManager.FLAG_VIBRATE
            )
            audioManager!!.setStreamVolume(
                AudioManager.STREAM_ALARM,
                alarmVolume,
                AudioManager.FLAG_VIBRATE
            )

            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("AutoSound Triggered")
                .setContentText("Ringer mode set to " + ringerMode.toString())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true)

            val notification = notificationBuilder.build()
            notificationManager!!.notify(1010, notification)
            Log.d(TAG, "onCheckedChanged: Receiver end")

//            val notificationBuilder = NotificationCompat.Builder(context, channelId)
//                .setSmallIcon(R.drawable.app_icon)
//                .setContentTitle("AutoSound Triggered")
//                .setContentText("Ringer mode set to " + ringerMode.toString())
//                .setAutoCancel(true) as NotificationCompat.Builder
//            notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
//            notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
//            notificationBuilder.setCustomBigContentView(setupCustomView(context, true))
//            notificationBuilder.setContent(setupCustomView(context, false))
//            val notification = notificationBuilder.build()
//            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
//            notificationManager!!.notify(1010, notification)
//            Log.d(TAG, "onCheckedChanged: Receiver end")
        }
    }

    /*
//    private fun setupCustomView(context: Context, bigView: Boolean): RemoteViews {
//        val remoteViews = RemoteViews(context.packageName, R.layout.notification)
//
//        //Setting title and content of notification's CustomView
//        remoteViews.setTextViewText(R.id.notification_title_txt, "AutoSound Triggered")
//        remoteViews.setTextViewText(
//            R.id.notification_content_txt,
//            "Ringer mode set to " + ringerMode.toString()
//        )
//        if (!bigView) {
//            remoteViews.setViewVisibility(R.id.trigger_card_content_layout, View.GONE)
//        } else {
//            //Setting volume ProgressBars
//            if (ringerMode != RingerMode.Normal) {
//                remoteViews.setViewVisibility(R.id.ringer_volume_tv, View.GONE)
//                remoteViews.setViewVisibility(R.id.ringer_volume_pbar, View.GONE)
//            } else {
//                remoteViews.setProgressBar(
//                    R.id.ringer_volume_pbar,
//                    audioManager!!.getStreamMaxVolume(AudioManager.STREAM_RING),
//                    ringerVolume,
//                    false
//                )
//            }
//            remoteViews.setProgressBar(
//                R.id.media_volume_pbar,
//                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
//                mediaVolume,

//                false
//            )
//            remoteViews.setProgressBar(
//                R.id.alarm_volume_pbar,
//                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_ALARM),
//                alarmVolume,
//                false
//            )
//        }
//        return remoteViews
//    }

     */

}