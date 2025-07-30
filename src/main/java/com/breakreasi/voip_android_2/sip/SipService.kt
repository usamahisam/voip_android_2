package com.breakreasi.voip_android_2.sip

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import com.breakreasi.voip_android_2.voip.Voip
import org.pjsip.pjsua2.IpChangeParam

class SipService : Service() {

    private val binder: LocalBinder = LocalBinder()
    val host: String = "voip.jasvicall.my.id"
    val port: Int = 5160
    var displayName: String = ""
    var username: String = ""
    var password: String = ""
    lateinit var sipEngine: SipEngine
    lateinit var sipRest: SipRest
    var sipAccount: SipAccount? = null
    lateinit var sipAudio: SipAudio
    lateinit var sipCamera: SipCamera
    lateinit var sipVideo: SipVideo
    lateinit var sipVoicemail: SipVoicemail
    lateinit var voip: Voip

    lateinit var connectivityManager: ConnectivityManager

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
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        connectivityManager.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Handler(Looper.getMainLooper()).post {
                    ipChange()
                }
            }
        });
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun auth(voip: Voip, displayName: String, username: String, password: String, destination: String, withVideo: Boolean) {
        this.voip = voip
        this.displayName = displayName
        this.username = username
        this.password = password
        deleteAccount()
        sipAccount = SipAccount(this)
        sipAccount?.auth(host, port, displayName, username, password, destination, withVideo)
    }

    fun reAuth() {
//      deleteAccount()
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
        if (sipAccount == null) return
        if (sipAccount?.call == null) return
        try {
            sipAccount?.call?.let {
                it.delete()
                sipAccount?.call = null
            }
        } catch (_: Exception) {
        }
    }

    private fun deleteAccount() {
        if (sipAccount == null) return
        try {
            sipAccount?.destroy()
//            sipAccount?.delete()
            sipAccount = null
        } catch (_: Exception) {
        }
    }

    private fun ipChange() {
        if (!sipEngine.isLibStarted) return
        try {
            sipEngine.endpoint?.handleIpChange(IpChangeParam())
        } catch (e: Exception) {
            Log.d("BNASBDNVBN", "e: ${e.message}")
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