package com.breakreasi.voip_android_2.sip

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.SurfaceView
import com.breakreasi.voip_android_2.voip.VoipCallback

class SipService : Service() {

    private val binder: LocalBinder = LocalBinder()
    private val host: String = "voip.jasvicall.my.id"
    private val port: Int = 5160
    lateinit var sipEngine: SipEngine
    lateinit var sipAccount: SipAccount
    lateinit var sipAudio: SipAudio
    lateinit var sipCamera: SipCamera
    lateinit var sipVideo: SipVideo

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): SipService = this@SipService
    }

    override fun onCreate() {
        super.onCreate()
        sipEngine = SipEngine(this)
        sipEngine.init(port)
        sipAudio = SipAudio(this)
        sipCamera = SipCamera(this)
        sipVideo = SipVideo(this)
        sipEngine.configures()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun auth(displayName: String, username: String, password: String, destination: String, withVideo: Boolean) {
        sipAccount = SipAccount(this).apply {
            auth(host, port, displayName, username, password, destination, withVideo)
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

    fun mic() {
        sipAudio.mic()
    }

    fun mute() {
        sipAudio.mute()
    }

    fun setSpeakerphoneOn(on: Boolean) {
        sipAudio.setSpeaker(on)
    }

    fun switchCamera(isFrontCamera: Boolean) {
        sipCamera.switchCamera(isFrontCamera)
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