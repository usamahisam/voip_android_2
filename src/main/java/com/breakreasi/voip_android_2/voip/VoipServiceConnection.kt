package com.breakreasi.voip_android_2.voip

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import com.breakreasi.voip_android_2.agora.AgoraService
import com.breakreasi.voip_android_2.sip.SipService

class VoipServiceConnection(
    private val voip: Voip
) : ServiceConnection {
    var voipNotificationService: VoipNotificationService? = null
    var sipService: SipService? = null
    var agoraService: AgoraService? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (name?.shortClassName == SipService::class.java.name) {
            val binder: SipService.LocalBinder = service as SipService.LocalBinder
            sipService = binder.getService()
            VoipManager.voip = voip
            sipService?.auth(voip, voip.displayName, voip.username, voip.password, voip.destination, voip.withVideo)
        } else if (name?.shortClassName == AgoraService::class.java.simpleName) {
            val binder: AgoraService.LocalBinder = service as AgoraService.LocalBinder
            agoraService = binder.getService()
            agoraService?.start(voip, voip.displayName, voip.channel, voip.userToken, voip.withVideo)
        } else if (name?.shortClassName == VoipNotificationService::class.java.simpleName) {
            val binder: VoipNotificationService.LocalBinder = service as VoipNotificationService.LocalBinder
            voipNotificationService = binder.getService()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        if (name?.shortClassName == SipService::class.java.simpleName) {
            sipService = null
        } else if (name?.shortClassName == AgoraService::class.java.simpleName) {
            agoraService = null
        }
    }

    fun start() {
        VoipManager.voip = voip
        if (sipService != null) {
            sipService?.auth(voip, voip.displayName, voip.username, voip.password, voip.destination, voip.withVideo)
        } else {
            startService(SipService::class.java)
        }
        if (agoraService != null) {
            agoraService?.start(voip, voip.displayName, voip.channel, voip.userToken, voip.withVideo)
        } else {
            startService(AgoraService::class.java)
        }
    }

    private fun startService(cls: Class<*>) {
        val i = Intent(voip.context, cls)
        voip.context.bindService(i, this, Context.BIND_ABOVE_CLIENT)
        voip.context.startService(i)
    }

    fun stop() {
        stopService(SipService::class.java)
        stopService(AgoraService::class.java)
    }

    private fun stopService(cls: Class<*>) {
        val i = Intent(voip.context, cls)
        voip.context.unbindService(this)
        voip.context.stopService(i)
    }

    fun startServiceNotification(type: VoipType, displayName: String, withVideo: Boolean, token: String) {
        VoipManager.voip = voip
        val i = Intent(voip.context, VoipNotificationService::class.java)
        i.setAction("notificationCallService")
        i.putExtra("type", type.name)
        i.putExtra("displayName", displayName)
        i.putExtra("withVideo", withVideo)
        i.putExtra("token", token)
        i.putExtra("with_fullscreen_intent", true)
        voip.context.bindService(i, this, Context.BIND_ABOVE_CLIENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            voip.context.startForegroundService(i)
        } else {
            voip.context.startService(i)
        }
    }

    fun startServiceVoicemailNotification(from: String, url: String) {
        val i = Intent(voip.context, VoipNotificationService::class.java)
        i.setAction("receiveVoicemail")
        i.putExtra("from", from)
        i.putExtra("url", url)
        voip.context.bindService(i, this, Context.BIND_ABOVE_CLIENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            voip.context.startForegroundService(i)
        } else {
            voip.context.startService(i)
        }
    }

    fun stopServiceNotification() {
        try {
            stopService(VoipNotificationService::class.java)
        } catch (_: Exception) {
        }
    }
}