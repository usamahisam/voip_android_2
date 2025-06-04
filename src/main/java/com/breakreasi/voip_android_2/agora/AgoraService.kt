package com.breakreasi.voip_android_2.agora

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.SurfaceView
import com.breakreasi.voip_android_2.voip.Voip
import io.agora.rtc2.Constants

class AgoraService: Service(), AgoraIEventListener {
    private var agoraEngine: AgoraEngine? = null
    lateinit var voip: Voip
    private var isCallActive: Boolean = false

    private val binder: AgoraService.LocalBinder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): AgoraService = this@AgoraService
    }

    override fun onCreate() {
        super.onCreate()
        agoraEngine = AgoraEngine(this)
        agoraEngine?.registerEventListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun start(voip: Voip, displayName: String, channel: String, userToken: String, withVideo: Boolean) {
        this.voip = voip
        this.isCallActive = false
        agoraEngine?.start(voip, displayName, channel, userToken, withVideo)
    }

    fun callIsOn(): Boolean {
        return isCallActive
    }

    fun accept() {
        agoraEngine?.accept()
    }

    fun decline() {
        agoraEngine?.hangup()
    }

    fun mic() {
        agoraEngine?.setMute(false)
    }

    fun mute() {
        agoraEngine?.setMute(true)
    }

    fun setSpeakerphoneOn(on: Boolean) {
        agoraEngine?.setLoudspeaker(on)
    }

    fun switchCamera() {
        agoraEngine?.switchCamera()
    }

    fun videoSurfaceLocal(surface: SurfaceView) {
        agoraEngine?.setLocalVideo(surface)
    }

    fun videoSurfaceRemote(surface: SurfaceView) {
        agoraEngine?.setRemoteVideo(surface)
    }

    fun toggleRemoteSurface() {
    }

    override fun onDestroy() {
        agoraEngine?.removeEventListener(this)
        agoraEngine?.destroy()
        super.onDestroy()
    }

    override fun onAgoraStatus(status: String?) {
    }

    override fun onJoinChannelSuccess(
        channel: String?,
        uid: Int,
        elapsed: Int
    ) {
    }

    override fun onRejoinChannelSuccess(
        channel: String?,
        uid: Int,
        elapsed: Int
    ) {
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        isCallActive = true
        voip.notifyCallStatus("connected")
        if (agoraEngine!!.withVideo) {
            agoraEngine?.startRemoteVideo(uid)
            agoraEngine?.startLocalVideo()
        }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        isCallActive = false
        voip.notifyCallStatus("disconnected")
        agoraEngine?.destroy()
    }

    override fun onConnectionStateChanged(status: Int, reason: Int) {
        if (status == Constants.CONNECTION_STATE_DISCONNECTED) {
            isCallActive = false
            voip.notifyCallStatus("disconnected")
            agoraEngine?.destroy()
        }
    }

    override fun onPeersOnlineStatusChanged(map: Map<String?, Int?>?) {
    }

    override fun onError(err: Int) {
    }
}