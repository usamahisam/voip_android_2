package com.breakreasi.voip_android_2.sip

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.SurfaceView
import com.breakreasi.voip_android_2.voip.Voip

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
    lateinit var sipVoicemail: SipVoicemail
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
        sipVoicemail = SipVoicemail(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun auth(voip: Voip, displayName: String, username: String, password: String, destination: String, withVideo: Boolean) {
        this.voip = voip
        if (sipAccount == null) {
            sipAccount = SipAccount(this)
        }
        sipAccount?.auth(host, port, displayName, username, password, destination, withVideo)
    }

    fun call(user: String, withVideo: Boolean) {
        sipRest.getUser(user, object : SipRestResponseCallback<SipRestResponse> {
            override fun onResponse(response: SipRestResponse?) {
                if (response != null && response.status == "success") {
                    sipAccount!!.destination = response.msg!!
                    sipAccount!!.newCall().makeCall(sipAccount!!.destination, withVideo)
                } else {
                    voip.notifyCallStatus("disconnected")
                }
            }
        })
    }

    fun callIsOn(): Boolean {
        if (sipAccount == null) return false
        if (sipAccount!!.call == null) return false
        return sipAccount!!.checkIsCall()
    }

    fun accept() {
        if (sipAccount == null) return
        if (sipAccount!!.call == null) return
        sipAccount!!.call!!.accept()
    }

    fun decline() {
        if (sipAccount == null) {
            voip.notifyCallStatus("disconnected")
            return
        }
        if (sipAccount!!.call == null) {
            voip.notifyCallStatus("disconnected")
            return
        }
        if (sipAccount!!.call!!.isCall) {
            sipAccount!!.call?.end()
        } else {
            sipAccount!!.call?.decline()
        }
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

    fun startRecVoicemail(from: String) {
        sipVoicemail.startRecord(from, sipAccount!!.destination)
    }

    fun stopRecVoicemail() {
        sipVoicemail.stopRecord()
    }

    fun sendVoicemail() {
        sipVoicemail.send()
    }

    private fun deleteCall() {
        try {
            sipAccount?.call?.let {
                it.delete()
                sipAccount = null
            }
        } catch (_: Exception) {
        }
    }

    private fun deleteAccount() {
        try {
            sipAccount?.let {
                it.destroy()
                sipAccount = null
            }
        } catch (_: Exception) {
        }
    }

    fun destroy() {
//        stopSelf()
        try {
            voip.notifyCallStatus("disconnected")
        } catch (_: Exception) {
        }
        sipVideo.destroy()
        deleteCall()
//        deleteAccount()
    }

    override fun onDestroy() {
//        Log.e("ABCDEF", "Destroy")
//        deleteCall()
        deleteAccount()
        sipEngine.destroy()
        super.onDestroy()
    }
}