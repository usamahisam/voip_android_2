package com.breakreasi.voip_android_2.voip

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.breakreasi.voip_android_2.history.HistoryPreferences

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
        if (action == "notificationCallService") {
            calling(type!!, displayName!!, withVideo!!, token!!)
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
        }
        return START_STICKY
    }

    fun calling(type: String, displayName: String, withVideo: Boolean, token: String) {
        val notificationCall = VoipNotificationCall(this)
        val notification = notificationCall.buildNotifyCall(type, displayName, withVideo, token)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1092, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(1092, notification)
        }
    }


}