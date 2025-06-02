package com.breakreasi.voip_android_2.agora

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class AgoraService: Service() {
    private var agoraEngine: AgoraEngine? = null

    private val binder: AgoraService.LocalBinder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): AgoraService = this@AgoraService
    }

    override fun onCreate() {
        super.onCreate()
        agoraEngine = AgoraEngine(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        agoraEngine?.destroy()
        super.onDestroy()
    }

    fun configure(displayName: String, channel: String, userToken: String, withVideo: Boolean) {
        agoraEngine?.configure(displayName, channel, userToken, withVideo)
    }
}