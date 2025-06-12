package com.breakreasi.voip_android_2.voip

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.breakreasi.voip_android_2.R
import java.util.Calendar
import androidx.core.net.toUri


class VoipNotificationCall(
    private val context: Context
) {
    private val CHANNEL_ID: String = "VOIP_ANDROID_123456"
    private val CHANNEL_ID_2: String = "VOIP_ANDROID_983479875"

    fun notificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Call Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Channel for incoming VOIP calls"
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotifyCall(
        type: String,
        displayName: String,
        withVideo: Boolean,
        token: String
    ): Notification {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        notificationChannel(CHANNEL_ID)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
        builder.setContentTitle("Pemanggilan dari $displayName")
        builder.setContentText("$displayName sedang memanggil Anda")
        builder.setSmallIcon(R.drawable.btn_startcall_normal)
        builder.setOngoing(true)
        builder.setAutoCancel(false)

        builder.setTicker("CALL_STATUS")
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setWhen(Calendar.getInstance().getTimeInMillis())
        builder.setCategory(NotificationCompat.CATEGORY_CALL)
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setFullScreenIntent(null, true)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        val fullScreenIntent = Intent(context, VoipIncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("type", type)
            putExtra("displayName", displayName)
            putExtra("withVideo", withVideo)
            putExtra("token", token)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            2,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setFullScreenIntent(fullScreenPendingIntent, true)

        val jawabIntent = Intent(context, VoipNotificationService::class.java)
        jawabIntent.setAction("acceptCall")
        jawabIntent.putExtra("type", type)
        jawabIntent.putExtra("displayName", displayName)
        jawabIntent.putExtra("withVideo", withVideo)
        jawabIntent.putExtra("token", token)
        val jawabPendingIntent = PendingIntent.getService(
            context,
            0,
            jawabIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tolakIntent = Intent(context, VoipNotificationService::class.java)
        tolakIntent.setAction("declineCall")
        tolakIntent.putExtra("type", type)
        tolakIntent.putExtra("displayName", displayName)
        tolakIntent.putExtra("withVideo", withVideo)
        tolakIntent.putExtra("token", token)
        val tolakPendingIntent = PendingIntent.getService(
            context,
            1,
            tolakIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder.addAction(
            NotificationCompat.Action.Builder(
                IconCompat.createWithResource(context, R.drawable.btn_startcall),
                "JAWAB",
                jawabPendingIntent
            ).build()
        )

        builder.addAction(
            NotificationCompat.Action.Builder(
                IconCompat.createWithResource(context, R.drawable.btn_endcall),
                "TOLAK",
                tolakPendingIntent
            ).build()
        )
        if (am?.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            builder.setVibrate(longArrayOf(0, 1000, 1000))
        } else if (am?.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            builder.setVibrate(longArrayOf(0, 1000, 1000))
            VoipManager.voip!!.playRingtone();
        }
        val notification = builder.build()
        notification.flags =
            notification.flags or (Notification.FLAG_INSISTENT or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)
        return notification
    }

    fun buildNotifyVoicemail(
        from: String,
        url: String,
    ): Notification {
        notificationChannel(CHANNEL_ID_2)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID_2
        )
        builder.setContentTitle("Voicemail dari $from")
        builder.setContentText("Anda menerima voicemail dari $from")
        builder.setSmallIcon(R.drawable.btn_startcall_pressed)
        builder.setOngoing(false)
        builder.setAutoCancel(true)

        builder.setTicker("CALL_VOICEMAIL")
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setWhen(Calendar.getInstance().getTimeInMillis())
        builder.setTimeoutAfter(10000)
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE)
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notification = builder.build()
//        notification.flags = notification.flags or (Notification.FLAG_INSISTENT or Notification.FLAG_NO_CLEAR)
        return notification
    }

    fun cancel(id: Int) {
        try {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.cancel(id)
        } catch (_: Exception) {
        }
    }
}