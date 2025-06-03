package com.breakreasi.voip_android_2.voip

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import com.breakreasi.voip_android_2.tone.Tone

class Voip(
    val context: Context,
) {
    private val voipServiceConnection = VoipServiceConnection(this)
    private val voipNotificationCallbacks = mutableListOf<VoipNotificationCallback>()
    private val voipCallbacks = mutableListOf<VoipCallback>()
    var type: VoipType = VoipType.SIP
    var displayName: String = ""
    var username: String = ""
    var password: String = ""
    var destination: String = ""
    var channel: String = ""
    var userToken: String = ""
    var withVideo: Boolean = false
    var withNotification: Boolean = false
    var callFromNotification: Boolean = false
    private val tone: Tone = Tone()

    fun auth(displayName: String, username: String, password: String, destination: String, withVideo: Boolean, withNotification: Boolean) {
        this.type = VoipType.SIP
        this.displayName = displayName
        this.username = username
        this.password = password
        this.destination = destination
        this.withVideo = withVideo
        this.withNotification = withNotification
    }

    fun auth(displayName: String, channel: String, userToken: String, withVideo: Boolean, withNotification: Boolean) {
        this.type = VoipType.AGORA
        this.displayName = displayName
        this.channel = channel
        this.userToken = userToken
        this.withVideo = withVideo
        this.withNotification = withNotification
    }

    fun engineStart() {
        voipServiceConnection.start()
    }

    fun registerNotificationCallback(callback: VoipNotificationCallback) {
        voipNotificationCallbacks.add(callback)
    }

    fun unregisterNotificationCallback(callback: VoipNotificationCallback) {
        voipNotificationCallbacks.remove(callback)
    }

    fun notifyNotificationStatus(status: String) {
        try {
            voipNotificationCallbacks.forEach { it.onNotificationStatus(status) }
        } catch (_: Exception) {
        }
    }

    fun registerCallback(callback: VoipCallback) {
        voipCallbacks.add(callback)
    }

    fun unregisterCallback(callback: VoipCallback) {
        voipCallbacks.remove(callback)
    }

    fun notifyAccountStatus(status: String) {
        try {
            voipCallbacks.forEach { it.onAccountStatus(status) }
        } catch (_: Exception) {
        }
    }

    fun notifyCallStatus(status: String) {
        if (status == "ringing") {
            tone.start()
        } else {
            tone.checkAndStop()
        }
        try {
            voipCallbacks.forEach { it.onCallStatus(this, status) }
        } catch (_: Exception) {
        }
    }

    fun makeCall(destination: String, withVideo: Boolean) {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.call(destination, withVideo)
        } else if (type == VoipType.AGORA) {
            voipServiceConnection.agoraService?.call()
        }
    }

    fun accept() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.accept()
        } else if (type == VoipType.AGORA) {
//            voipServiceConnection.agoraService?.accept()
        }
    }

    fun decline() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.decline()
        } else if (type == VoipType.AGORA) {
//            voipServiceConnection.agoraService?.decline()
        }
    }

    fun setMute(mute: Boolean) {
        if (type == VoipType.SIP) {
            if (mute) {
                voipServiceConnection.sipService?.mute()
            } else {
                voipServiceConnection.sipService?.mic()
            }
        } else if (type == VoipType.AGORA) {
        }
    }

    fun switchCamera() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.switchCamera()
        } else if (type == VoipType.AGORA) {
        }
    }

    fun toggleRemoteSurface() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.toggleRemoteSurface()
        } else if (type == VoipType.AGORA) {
        }
    }

    fun videoSurface(localSurface: SurfaceView, remoteSurface: SurfaceView) {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.videoSurfaceLocal(localSurface)
            voipServiceConnection.sipService?.videoSurfaceRemote(remoteSurface)
        } else if (type == VoipType.AGORA) {
        }
    }

    fun notificationCallService(type: VoipType, displayName: String, withVideo: Boolean, token: String) {
        if (!withNotification) return
        VoipManager.voip = this
        voipServiceConnection.startServiceNotification(type, displayName, withVideo, token)
    }

    fun stopNotificationCallService() {
        if (!withNotification) return
        VoipManager.voip = this
        voipServiceConnection.stopServiceNotification()
    }

    fun handlerNotificationAccept() {
        voipServiceConnection.stopServiceNotification()
        callFromNotification = true
        notifyNotificationStatus("notification_accept")
    }

    fun handlerNotificationDecline() {
        voipServiceConnection.stopServiceNotification()
        callFromNotification = true
        notifyNotificationStatus("notification_decline")
    }

    fun destroy() {
        voipServiceConnection.stop()
    }
}