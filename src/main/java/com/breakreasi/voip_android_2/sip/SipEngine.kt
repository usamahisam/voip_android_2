package com.breakreasi.voip_android_2.sip

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import org.pjsip.PjCameraInfo2
import org.pjsip.pjsua2.EpConfig
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
    private var epConfig: EpConfig? = null

    fun init(udpPort: Int? = null) {
        cm = sipService.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        am = sipService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            PjCameraInfo2.SetCameraManager(cm)
            endpoint = SipEndpoint().apply {
                libCreate()
            }
            epConfig = EpConfig().apply {
                uaConfig.userAgent = String.format(
                    "VOIP SIP Client/%s (%s %s; Android %s)",
                    1.1,
                    Build.MANUFACTURER,
                    Build.MODEL,
                    Build.VERSION.RELEASE
                )
                medConfig.hasIoqueue = true
                medConfig.clockRate = 16000
                medConfig.quality = 10
                medConfig.ecOptions = 1
                medConfig.ecTailLen = 200
                medConfig.threadCnt = 2
            }
            endpoint?.libInit(epConfig)
            if (udpPort != null) {
                val udpTransport = TransportConfig().apply {
                    qosType = pj_qos_type.PJ_QOS_TYPE_VOICE
                    port = udpPort.toLong()
                }
                endpoint?.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, udpTransport)
            }
            endpoint?.libStart()
        } catch (_: Exception) {
        }
    }

    fun configures() {
        sipService.sipAudio.configure()
        sipService.sipCamera.configure()
        sipService.sipVideo.configure()
    }

    fun destroy() {
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
