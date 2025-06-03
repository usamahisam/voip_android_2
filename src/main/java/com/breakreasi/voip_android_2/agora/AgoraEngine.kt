package com.breakreasi.voip_android_2.agora

import android.view.SurfaceView
import com.breakreasi.voip_android_2.rests.ResponseInitConfigModel
import com.breakreasi.voip_android_2.rests.ResponseModel
import com.breakreasi.voip_android_2.rests.Rests
import com.breakreasi.voip_android_2.voip.Voip
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Random

class AgoraEngine(
    private val agoraService: AgoraService
) {
    private var rests: Rests? = null
    private var rtcEngine: RtcEngine? = null
    private var displayName: String? = null
    private var channel: String? = null
    private var userToken: String? = null
    private var uid = 0
    private var withVideo: Boolean = false
    private var eventListener: AgoraEngineEventListener? = null
    private var localVideo: SurfaceView? = null
    private var remoteVideo: SurfaceView? = null
    lateinit var voip: Voip

    init {
        rests = Rests()
        uid = generateRandom(8).toInt()
        eventListener = AgoraEngineEventListener()
    }

    private fun generateRandom(length: Int): Long {
        val random = Random()
        val digits = CharArray(length)
        digits[0] = (random.nextInt(9) + '1'.code).toChar()
        for (i in 1..<length) {
            digits[i] = (random.nextInt(10) + '0'.code).toChar()
        }
        return String(digits).toLong()
    }

    fun configure(voip: Voip, displayName: String, channel: String, userToken: String, withVideo: Boolean) {
        this.voip = voip
        this.displayName = displayName
        this.channel = channel
        this.userToken = userToken
        this.withVideo = withVideo
        rests!!.initConfig {
            if (it != null) {
                configureRtc(it.agora_app_id)
            } else {
                eventListener!!.onAgoraStatus("failed")
            }
        }
    }

    fun configureRtc(agoraId: String) {
        try {
            val config = RtcEngineConfig()
            config.mContext = agoraService
            config.mAppId = agoraId
            config.mEventHandler = eventListener
            config.mAutoRegisterAgoraExtensions = false
            rtcEngine = RtcEngine.create(config).apply {
                setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION_1v1)
                setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            }
        } catch (_: Exception) {
        }
    }

    fun registerEventListener(listener: AgoraIEventListener) {
        eventListener!!.registerEventListener(listener)
    }

    fun removeEventListener(listener: AgoraIEventListener) {
        eventListener!!.removeEventListener(listener)
    }

    fun setModeCall(withVideo: Boolean) {
        if (rtcEngine == null) return
        if (withVideo) {
            rtcEngine?.enableVideo()
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
            rtcEngine?.setEnableSpeakerphone(true)
        } else {
            rtcEngine?.enableAudio()
            rtcEngine?.disableVideo()
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(false)
            rtcEngine?.setEnableSpeakerphone(false)
        }
    }

    fun call() {
//        rests.makeCall(
//            voip.destination,
//            voip.displayName,
//            userToken,
//            withVideo,
//            "agora"
//        ) {  }
    }

    fun joinChannel() {
        if (rtcEngine == null) return
        rtcEngine!!.joinChannel(null, channel, "", uid)
    }

    fun setVideoConfiguration() {
        if (rtcEngine == null) return
        rtcEngine!!.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_480x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.COMPATIBLE_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            )
        )
    }

    fun setRemoteVideo(surfaceView: SurfaceView) {
        remoteVideo = surfaceView
    }

    fun setLocalVideo(surfaceView: SurfaceView) {
        localVideo = surfaceView
    }

    fun startLocalVideo() {
        if (rtcEngine == null) return
        rtcEngine!!.setupLocalVideo(VideoCanvas(localVideo, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun startRemoteVideo(rUid: Int) {
        if (rtcEngine == null) return
        rtcEngine!!.setupRemoteVideo(VideoCanvas(remoteVideo, VideoCanvas.RENDER_MODE_HIDDEN, rUid))
    }

    fun startPreview() {
        if (rtcEngine == null) return
        rtcEngine!!.startPreview()
    }

    fun unsetLocalVideo() {
        if (rtcEngine == null) return
        rtcEngine!!.setupLocalVideo(null)
    }

    fun unsetRemoteVideo() {
        if (rtcEngine == null) return
        rtcEngine!!.setupRemoteVideo(null)
    }

    fun accept() {
        if (rtcEngine == null) return
        rests!!.acceptCall (
            userToken,
            channel
        ) {
            if (it != null) {
                setVideoConfiguration()
                startPreview()
                joinChannel()
            }
        }
    }

    fun hangup() {
        if (rtcEngine != null) {
            rtcEngine!!.stopPreview()
            rtcEngine!!.leaveChannel()
        }
        rests!!.rejectCall(
            userToken
        ) {
            if (it != null) {
            }
        }
    }

    fun destroy() {
        Thread {
            try {
                if (rtcEngine != null) {
                    RtcEngine.destroy()
                    rtcEngine = null
                }
            } catch (_: Exception) {
            }
        }.start()
    }
}