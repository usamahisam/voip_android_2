package com.breakreasi.voip_android_2.voip

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.SurfaceView
import androidx.core.net.toUri
import com.breakreasi.voip_android_2.R
import com.breakreasi.voip_android_2.database.HistoryPreferences
import com.breakreasi.voip_android_2.database.VoicemailModel
import com.breakreasi.voip_android_2.tone.Tone

class Voip(
    val context: Context,
) {
    private val voipServiceConnection = VoipServiceConnection(this)
    private val voipNotificationCallbacks = mutableListOf<VoipNotificationCallback>()
    private val voipCallbacks = mutableListOf<VoipCallback>()
    private val voipVoicemailCallback = mutableListOf<VoipVoicemailCallback>()
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
    val tone: Tone = Tone()
    private var ringtone: Ringtone? = null

    fun engineStart(displayName: String, username: String, password: String, destination: String, withVideo: Boolean, withNotification: Boolean) {
        this.type = VoipType.SIP
        this.displayName = displayName
        this.username = username
        this.password = password
        this.destination = destination
        this.withVideo = withVideo
        this.withNotification = withNotification
        voipServiceConnection.start(type)
    }

    fun engineStart(displayName: String, channel: String, userToken: String, withVideo: Boolean, withNotification: Boolean) {
        this.type = VoipType.AGORA
        this.displayName = displayName
        this.channel = channel
        this.userToken = userToken
        this.withVideo = withVideo
        this.withNotification = withNotification
        voipServiceConnection.start(type)
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
        try {
            voipCallbacks.forEach { it.onCallStatus(this, status) }
        } catch (_: Exception) {
        }
    }

    fun registerVoicemailCallback(callback: VoipVoicemailCallback) {
        voipVoicemailCallback.add(callback)
    }

    fun unregisterVoicemailCallback(callback: VoipVoicemailCallback) {
        voipVoicemailCallback.remove(callback)
    }

    fun notifyVoicemailRecord(status: String) {
        try {
            voipVoicemailCallback.forEach {
                it.onVoicemailRecordStatus(status)
            }
        } catch (_: Exception) {
        }
    }

    fun notifyVoicemailTimer(seconds: Long) {
        try {
            voipVoicemailCallback.forEach {
                it.onVoicemailRecordTimer(seconds)
            }
        } catch (_: Exception) {
        }
    }

    fun callIsOn(): Boolean {
        try {
            if (voipServiceConnection.sipService != null) {
                return voipServiceConnection.sipService!!.callIsOn()
            }
            return false
        } catch (_: Exception) {
            return false
        }
    }

    fun makeCall(destination: String, withVideo: Boolean) {
        if (type == VoipType.SIP) {
            VoipManager.voip = this
            voipServiceConnection.sipService?.call(destination, withVideo)
        } else if (type == VoipType.AGORA) {
            // still disable this function
            // voipServiceConnection.agoraService?.call()
        }
    }

    fun accept() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.accept()
        } else if (type == VoipType.AGORA) {
            voipServiceConnection.agoraService?.accept()
        }
    }

    fun decline() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.decline()
        } else if (type == VoipType.AGORA) {
            voipServiceConnection.agoraService?.decline()
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
            if (mute) {
                voipServiceConnection.agoraService?.mute()
            } else {
                voipServiceConnection.agoraService?.mic()
            }
        }
    }

    fun setSpeakerphoneOn(on: Boolean) {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.setSpeakerphoneOn(on)
        } else if (type == VoipType.AGORA) {
            // woi
        }
    }

    fun switchCamera() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.switchCamera()
        } else if (type == VoipType.AGORA) {
            voipServiceConnection.agoraService?.switchCamera()
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
            if (voipServiceConnection.sipService != null) {
                voipServiceConnection.sipService?.videoSurfaceLocal(localSurface)
                voipServiceConnection.sipService?.videoSurfaceRemote(remoteSurface)
            } else {
                val timer = object : CountDownTimer(1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }
                    override fun onFinish() {
                        videoSurface(localSurface, remoteSurface)
                    }
                }
                timer.start()
            }
        } else if (type == VoipType.AGORA) {
            voipServiceConnection.agoraService?.videoSurfaceRemote(remoteSurface)
            val timer = object : CountDownTimer(1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }
                override fun onFinish() {
                    voipServiceConnection.agoraService?.videoSurfaceLocal(localSurface)
                }
            }
            timer.start()
        }
    }

    fun startVoicemail(from: String) {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.startRecVoicemail(from)
        }
    }

    fun stopVoicemail() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.stopRecVoicemail()
        }
    }

    fun sendVoicemail() {
        if (type == VoipType.SIP) {
            voipServiceConnection.sipService?.sendVoicemail()
        }
    }

    fun getVoicemailList(): MutableList<VoicemailModel>? {
        return voipServiceConnection.sipService?.sipVoicemail!!.getList()
    }

    fun notificationCallService(type: VoipType, displayName: String, withVideo: Boolean, token: String) {
        if (!withNotification) return
        if (callIsOn()) return
        VoipManager.voip = this
        voipServiceConnection.startServiceNotification(type, displayName, withVideo, token)
    }

    fun stopNotificationCallService() {
        if (!withNotification) return
        stopRingtone()
        VoipManager.voip = this
        voipServiceConnection.stopServiceNotification()
    }

    fun handlerNotificationAccept() {
        stopRingtone()
        voipServiceConnection.stopServiceNotification()
        callFromNotification = true
        notifyNotificationStatus("notification_accept")
    }

    fun handlerNotificationDecline(isMissed: Boolean = false) {
        stopRingtone()
        decline()
        if (isMissed) {
            notificationMissedCall(displayName)
            HistoryPreferences.save(context, displayName, "Missed Call")
        } else {
            HistoryPreferences.save(context, displayName, "Call Declined")
            voipServiceConnection.stopServiceNotification()
        }
        callFromNotification = true
        notifyNotificationStatus("notification_decline")
    }

    fun playRingtone() {
        stopRingtone()
        try {
            val soundUri = (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    context.packageName + "/" + R.raw.basic_ring).toUri()
            ringtone = RingtoneManager.getRingtone(context, soundUri)
            if (ringtone != null && !ringtone!!.isPlaying) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone!!.isLooping = true
                }
                ringtone!!.play()
            }
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun stopRingtone() {
        if (ringtone != null) {
            try {
                if (ringtone!!.isPlaying) ringtone!!.stop()
            } catch (_: Exception) {
            }
            ringtone = null
        }
    }

    fun notificationVoicemailService(from: String, url: String) {
        VoipManager.voip = this
        voipServiceConnection.startServiceVoicemailNotification(from, url)
    }

    private fun notificationMissedCall(from: String) {
        VoipManager.voip = this
        voipServiceConnection.startServiceMissedCallNotification(from)
    }

    private fun destroy() {
        voipServiceConnection.stop()
    }
}