package com.breakreasi.voip_android_2.sip

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.SurfaceView

class SipService : Service() {

    private val binder: LocalBinder = LocalBinder()
    private val host: String = "voip.jasvicall.my.id"
    private val port: Int = 5160
    private lateinit var sipEngine: SipEngine
    private lateinit var sipAccount: SipAccount
    var sipVideo: SipVideo = SipVideo()
    private val sipCallbacks = mutableListOf<SipCallback>()

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): SipService = this@SipService
    }

    override fun onCreate() {
        super.onCreate()
        sipEngine = SipEngine(this)
        sipEngine.init(port)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun registerCallback(callback: SipCallback) {
        sipCallbacks.add(callback)
    }

    fun unregisterCallback(callback: SipCallback) {
        sipCallbacks.remove(callback)
    }

    fun notifyAccountStatus(status: String) {
        sipCallbacks.forEach { it.onAccountStatus(status) }
    }

    fun notifyStatus(status: String) {
        sipCallbacks.forEach { it.onCallStatus(status) }
    }

    fun auth(displayName: String, username: String, password: String) {
        sipAccount = SipAccount(this).apply {
            auth(host, port, displayName, username, password)
            createAccount()
        }
    }

    fun call(user: String, withVideo: Boolean) {
        sipAccount.newCall().makeCall(user, withVideo)
    }

    fun accept() {
        sipAccount.call?.accept()
    }

    fun decline() {
        sipAccount.call?.decline()
    }

    fun hangup() {
        sipAccount.call?.decline();
    }

    fun videoSurfaceLocal(surface: SurfaceView) {
        sipVideo.addLocalVideoSurface(surface)
    }

    fun videoSurfaceRemote(surface: SurfaceView) {
        sipVideo.addRemoteVideoSurface(surface)
    }

    override fun onDestroy() {
        sipAccount.logout()
        sipEngine.destroy()
        super.onDestroy()
    }
}