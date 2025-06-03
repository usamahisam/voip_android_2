package com.breakreasi.voip_android_2.sip

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.SurfaceView
import com.breakreasi.voip_android_2.voip.Voip
import com.breakreasi.voip_android_2.voip.VoipCallback

class SipService : Service() {

    private val binder: LocalBinder = LocalBinder()
    val host: String = "voip.jasvicall.my.id"
    val port: Int = 5160
    lateinit var sipEngine: SipEngine
    lateinit var sipRest: SipRest
    var sipAccount: SipAccount? = null
    lateinit var sipAudio: SipAudio
    lateinit var sipCamera: SipCamera
    lateinit var sipVideo: SipVideo
    lateinit var voip: Voip

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): SipService = this@SipService
    }

    override fun onCreate() {
        super.onCreate()
        sipEngine = SipEngine(this)
        sipEngine.init(port)
        sipRest = SipRest(this)
        sipAudio = SipAudio(this)
        sipCamera = SipCamera(this)
        sipVideo = SipVideo(this)
        sipEngine.configures()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun auth(voip: Voip, displayName: String, username: String, password: String, destination: String, withVideo: Boolean) {
        this.voip = voip
        sipAccount = SipAccount(this)
        sipAccount?.auth(host, port, displayName, username, password, destination, withVideo)
    }

    fun call(user: String, withVideo: Boolean) {
        sipAccount!!.newCall().makeCall(user, withVideo)
    }

    fun callIsOn(): Boolean {
        if (sipAccount == null) return false
        if (sipAccount?.call == null) return false
        return sipAccount!!.checkIsCall()
    }

    fun accept() {
        sipAccount!!.call?.accept()
    }

    fun decline() {
        sipAccount!!.call?.decline()
    }

    fun hangup() {
        sipAccount!!.call?.decline();
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

    fun switchCamera() {
        sipCamera.switchCamera()
    }

    fun videoSurfaceLocal(surface: SurfaceView) {
        sipVideo.addLocalVideoSurface(surface)
    }

    fun videoSurfaceRemote(surface: SurfaceView) {
        sipVideo.addRemoteVideoSurface(surface)
    }

    fun toggleRemoteSurface() {
        sipVideo.toggleSurfaceRemoteFit()
    }

    override fun onDestroy() {
        sipAccount!!.logout()
        sipEngine.destroy()
        super.onDestroy()
    }
}