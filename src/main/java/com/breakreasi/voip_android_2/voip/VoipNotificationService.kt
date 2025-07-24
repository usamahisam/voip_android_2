package com.breakreasi.voip_android_2.voip

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder

class VoipNotificationService: Service() {
    private val binder: VoipNotificationService.LocalBinder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): VoipNotificationService = this@VoipNotificationService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var action = intent?.action
        var type = intent?.getStringExtra("type")
        var displayName = intent?.getStringExtra("displayName")
        var withVideo = intent?.getBooleanExtra("withVideo", false)
        var token = intent?.getStringExtra("token")
        var withFullscreenIntent = intent?.getBooleanExtra("with_fullscreen_intent", true)
        if (action == "notificationCallService") {
            calling(type!!, displayName!!, withVideo!!, token!!, withFullscreenIntent!!)
        } else if (action == "acceptCall") {
            if (VoipManager.voip != null) {
                if (type.equals(VoipType.SIP.name)) {
                    VoipManager.voip!!.type = VoipType.SIP
                } else if (type.equals(VoipType.AGORA.name)) {
                    VoipManager.voip!!.type = VoipType.AGORA
                }
                VoipManager.voip!!.handlerNotificationAccept()
            }
            stopSelf()
        } else if (action == "declineCall") {
            if (VoipManager.voip != null) {
                if (type.equals(VoipType.SIP.name)) {
                    VoipManager.voip!!.type = VoipType.SIP
                } else if (type.equals(VoipType.AGORA.name)) {
                    VoipManager.voip!!.type = VoipType.AGORA
                }
                VoipManager.voip!!.handlerNotificationDecline()
            }
            stopSelf()
        } else if (action == "receiveVoicemail") {
            val from = intent?.getStringExtra("from")
            val url = intent?.getStringExtra("url")
            voicemail(from!!, url!!)
        } else if (action == "missedCall") {
            val from = intent?.getStringExtra("from")
            missedCall(from!!)
        }
        return START_STICKY
    }

    private fun calling(type: String, displayName: String, withVideo: Boolean, token: String, withFullscreenIntent: Boolean) {
        val notificationCall = VoipNotificationCall(this)
        val notification = notificationCall.buildNotifyCall(type, displayName, withVideo, token, withFullscreenIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1092, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(1092, notification)
        }
    }

    private fun voicemail(from: String, url: String) {
        val notificationCall = VoipNotificationCall(this)
        val notification = notificationCall.buildNotifyVoicemail(from, url)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1099, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(1099, notification)
        }
    }

    private fun missedCall(from: String) {
        val notificationCall = VoipNotificationCall(this)
        val notification = notificationCall.buildNotifyCallTimeout(from)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1282, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(1282, notification)
        }
    }

}