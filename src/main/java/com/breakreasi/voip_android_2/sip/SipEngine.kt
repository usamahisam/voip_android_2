package com.breakreasi.voip_android_2.sip

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.util.Log
import org.pjsip.PjCameraInfo2
import org.pjsip.pjsua2.EpConfig
import org.pjsip.pjsua2.StringVector
import org.pjsip.pjsua2.TransportConfig
import org.pjsip.pjsua2.pj_qos_type
import org.pjsip.pjsua2.pjsip_transport_type_e
import java.util.Locale

class SipEngine(
    private val sipService: SipService
) {
    var cm: CameraManager? = null
    var am: AudioManager? = null
    var endpoint: SipEndpoint? = null
    var isLibStarted: Boolean = false
    private var epConfig: EpConfig? = null

    fun init(udpPort: Int? = null) {
        cm = sipService.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        am = sipService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            PjCameraInfo2.SetCameraManager(cm)
            endpoint = SipEndpoint(sipService).apply {
                libCreate()
            }
        } catch (_: Exception) {
        }
        try {
            val stunServers = StringVector().apply {
                // Google STUN Servers
                add("stun:stun.l.google.com:19302")
                add("stun:stun1.l.google.com:19302")
                add("stun:stun2.l.google.com:19302")
                add("stun:stun3.l.google.com:19302")
                add("stun:stun4.l.google.com:19302")

                // Twilio STUN Servers
                add("stun:global.stun.twilio.com:3478")

                // 3CX STUN Servers
                add("stun:stun.3cx.com:3478")

                // Other reliable public STUN
                add("stun:stun.stunprotocol.org:3478")
                add("stun:stun.sipnet.net:3478")
                add("stun:stun.ideasip.com:3478")
                add("stun:stun.ekiga.net:3478")
            }

            epConfig = EpConfig().apply {
                uaConfig.userAgent = String.format(
                    "VOIP SIP Client/%s (%s %s; Android %s)",
                    1.1,
                    Build.MANUFACTURER,
                    Build.MODEL,
                    Build.VERSION.RELEASE
                )
                uaConfig.stunServer = stunServers
                medConfig.hasIoqueue = true
                medConfig.clockRate = 16000
                medConfig.quality = 10
                medConfig.ecOptions = 1
                medConfig.ecTailLen = 200
                medConfig.threadCnt = 2
            }
            endpoint?.libInit(epConfig)
        } catch (e: Exception) {
            Log.e("hagsdhjaghd", "init", e)
        }
        try {
            if (udpPort != null) {
                val udpTransport = TransportConfig().apply {
                    qosType = pj_qos_type.PJ_QOS_TYPE_VOICE
                    port = udpPort.toLong()
                }
                endpoint?.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, udpTransport)
            }
            endpoint?.libRegisterThread("VOIP ANDROID 2")
            endpoint?.libStart()
            isLibStarted = true
        } catch (e: Exception) {
            Log.e("hagsdhjaghd", "init", e)
        }
    }

    fun configures() {
        sipService.sipAudio.configure()
        sipService.sipCamera.configure()
        sipService.sipVideo.configure()
    }

    fun destroy() {
        isLibStarted = false
        try {
            endpoint?.let {
                it.libDestroy()
                it.delete()
            }
        } catch (_: Exception) {
        } finally {
            endpoint = null
            epConfig?.delete()
            epConfig = null
        }
        System.gc()
    }
}
