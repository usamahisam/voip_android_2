package com.breakreasi.voip_android_2.sip

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import org.pjsip.pjsua2.*

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
                val priority = when {
                    codecId.startsWith("G729") -> 254.toShort()
                    codecId.startsWith("opus") -> 240.toShort()
                    codecId.startsWith("PCMA") -> 235.toShort()
                    codecId.startsWith("PCMU") -> 230.toShort()
                    else -> 0.toShort()
                }
                sipService.sipEngine.endpoint!!.codecSetPriority(codecId, priority)
            }
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to configure codecs", e)
        }
    }

    fun start(media: Media) {
        val audioMedia = AudioMedia.typecastFromMedia(media)
        try {
            val audDevManager = sipService.sipEngine.endpoint!!.audDevManager()
            if (audioMedia != null) {
                try {
                    audioMedia.adjustRxLevel(1.5f)
                    audioMedia.adjustTxLevel(1.5f)
                } catch (_: Exception) {}

                audioMedia.startTransmit(audDevManager.playbackDevMedia)
                audDevManager.captureDevMedia.startTransmit(audioMedia)
            }
            this.audioMedia = audioMedia
            sipService.sipEngine.am!!.mode = AudioManager.MODE_IN_COMMUNICATION
            mic()
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to start audio", e)
        }
    }

    fun stop() {
        try {
            sipService.sipEngine.am!!.mode = AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to stop audio", e)
        }

        var adm: AudDevManager? = null
        try {
            adm = sipService.sipEngine.endpoint!!.audDevManager()
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to stop audio", e)
        }

        if (adm == null) return
        if (audioMedia == null) return

//        try {
//            val captureMedia = adm.captureDevMedia
//            val playbackMedia = adm.playbackDevMedia
//            captureMedia?.stopTransmit(audioMedia)
//            if (playbackMedia != null) {
//                audioMedia!!.stopTransmit(playbackMedia)
//            }
//            audioMedia = null
//        } catch (e: Exception) {
//            Log.e("SipAudio", "Failed to stop audio", e)
//        }
    }

    fun mute() {
        if (audioMedia == null || audioMedia!!.portId < 0) return
        try {
            isMute = true
            audioMedia!!.adjustTxLevel(0.0f)
            val capture = sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia
            if (capture != null && capture.portId >= 0) {
                capture.adjustTxLevel(0.0f)
                capture.stopTransmit(audioMedia)
            }
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to mute audio", e)
        }
    }

    fun mic() {
        if (audioMedia == null || audioMedia!!.portId < 0) return
        try {
            isMute = false
            audioMedia!!.adjustTxLevel(1.5f)
            val capture = sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia
            if (capture != null && capture.portId >= 0) {
                capture.adjustTxLevel(1.5f)
                capture.startTransmit(audioMedia)
            }
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to enable mic", e)
        }
    }

    fun setSpeaker(isLoudSpeaker: Boolean) {
//        try {
//            sipService.sipEngine.endpoint!!.audDevManager().outputRoute =
//                if (isLoudSpeaker) pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_LOUDSPEAKER
//                else pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_DEFAULT
//        } catch (_: Exception) {}

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val targetDevice = if (isLoudSpeaker) {
                    sipService.sipEngine.am!!.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    }
                } else {
                    sipService.sipEngine.am!!.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                                it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                    }
                }
                targetDevice?.let {
                    sipService.sipEngine.am!!.setCommunicationDevice(it)
                }
            }
        } catch (_: Exception) {}

        try {
            @Suppress("DEPRECATION")
            sipService.sipEngine.am!!.setSpeakerphoneOn(isLoudSpeaker)
        } catch (_: Exception) {}
    }

    fun reconfigureAudioDevices(captureId: Int = 0, playbackId: Int = 0) {
        try {
            val adm = sipService.sipEngine.endpoint?.audDevManager()
            adm?.captureDev = captureId
            adm?.playbackDev = playbackId
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to reconfigure audio devices", e)
        }
    }
}
