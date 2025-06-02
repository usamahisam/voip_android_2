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
    private val context: Context
) : ServiceConnection {
    var sipService: SipService? = null
    var agoraService: AgoraService? = null
    private var displayName: String = ""
    private var username: String = ""
    private var password: String = ""
    private var destination: String = ""
    private var channel: String = ""
    private var userToken: String = ""
    private var withVideo: Boolean = false

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (name?.shortClassName == SipService::class.java.simpleName) {
            val binder: SipService.LocalBinder = service as SipService.LocalBinder
            sipService = binder.getService()
            sipService?.auth(displayName, username, password, "", withVideo)
        } else if (name?.shortClassName == AgoraService::class.java.simpleName) {
            val binder: AgoraService.LocalBinder = service as AgoraService.LocalBinder
            agoraService = binder.getService()
            agoraService?.configure(displayName, channel, userToken, withVideo)
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
        startService(SipService::class.java)
        startService(AgoraService::class.java)
    }

    private fun startService(cls: Class<*>) {
        val i = Intent(context, cls)
        context.bindService(i, this, Context.BIND_ABOVE_CLIENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.startForegroundService(i)
        } else {
            context.startService(i)
        }
    }

    fun stop() {
        stopService(SipService::class.java)
        stopService(AgoraService::class.java)
    }

    private fun stopService(cls: Class<*>) {
        val i = Intent(context, cls)
        context.unbindService(this)
        context.stopService(i)
    }

    fun sipAuth(displayName: String, username: String, password: String, destination: String, withVideo: Boolean) {
        this.displayName = displayName
        this.username = username
        this.password = password
        this.destination = destination
        this.withVideo = withVideo
    }

    fun agoraConfigure(displayName: String, channel: String, userToken: String, withVideo: Boolean) {
        this.displayName = displayName
        this.channel = channel
        this.userToken = userToken
        this.withVideo = withVideo
    }
}