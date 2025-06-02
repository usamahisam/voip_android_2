package com.breakreasi.voip_android_2.sip

import org.pjsip.pjsua2.AudDevManager
import org.pjsip.pjsua2.AudioMedia
import org.pjsip.pjsua2.CodecInfoVector2
import org.pjsip.pjsua2.Media
import org.pjsip.pjsua2.pjmedia_aud_dev_route

class SipAudio(
    private val sipService: SipService
) {
    private var audioMedia: AudioMedia? = null
    private var isMute = false

    fun configure() {
        try {
            val codecs: CodecInfoVector2 = sipService.sipEngine.endpoint!!.codecEnum2()
            for (codec in codecs) {
                val codecId = codec.codecId
                if (codecId.startsWith("G729")) {
                    sipService.sipEngine.endpoint!!.codecSetPriority(codecId, 254.toShort())
                } else if (codecId.startsWith("opus")) {
                    sipService.sipEngine.endpoint!!.codecSetPriority(codecId, 240.toShort())
                } else if (codecId.startsWith("PCMA")) {
                    sipService.sipEngine.endpoint!!.codecSetPriority(codecId, 235.toShort())
                } else if (codecId.startsWith("PCMU")) {
                    sipService.sipEngine.endpoint!!.codecSetPriority(codecId, 230.toShort())
                } else {
                    sipService.sipEngine.endpoint!!.codecSetPriority(codecId, 0.toShort())
                }
            }
        } catch (ignored: java.lang.Exception) {
        }
    }

    fun start(media: Media) {
        try {
            audioMedia = AudioMedia.typecastFromMedia(media)
        } catch (_: Exception) {
            return
        }
        try {
            sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia?.startTransmit(audioMedia)
            sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia?.adjustTxLevel(2.0f)
            sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia?.adjustRxLevel(2.0f)
            audioMedia?.startTransmit(sipService.sipEngine.endpoint?.audDevManager()?.playbackDevMedia)
            audioMedia?.adjustRxLevel(2.0f)
            audioMedia?.adjustTxLevel(2.0f)
        } catch (_: Exception) {
        }
    }

    fun stop() {
        try {
            sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia?.stopTransmit(audioMedia)
            audioMedia?.stopTransmit(sipService.sipEngine.endpoint?.audDevManager()?.playbackDevMedia)
            audioMedia = null
        } catch (_: Exception) {
        }
    }

    fun mute() {
        if (audioMedia == null) return
        try {
            isMute = true
            audioMedia!!.adjustTxLevel(0.0f)
            sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia?.adjustTxLevel(0.0f)
            sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia?.stopTransmit(audioMedia)
        } catch (_: Exception) {
        }
    }

    fun mic() {
        if (audioMedia == null) return
        try {
            isMute = false
            audioMedia!!.adjustTxLevel(2.0f)
            sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia?.adjustTxLevel(2.0f)
            sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia?.startTransmit(audioMedia)
        } catch (_: Exception) {
        }
    }

    fun setSpeaker(isLoudSpeaker: Boolean) {
        val audDevManager: AudDevManager = sipService.sipEngine.endpoint!!.audDevManager()
        try {
            if (isLoudSpeaker) {
                audDevManager.outputRoute = pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_LOUDSPEAKER
            } else {
                audDevManager.outputRoute = pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_EARPIECE
            }
        } catch (_: Exception) {
        }
    }
}