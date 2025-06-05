package com.breakreasi.voip_android_2.sip

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
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
            val audDevManager: AudDevManager = sipService.sipEngine.endpoint!!.audDevManager()
            if (audioMedia != null) {
                try {
                    audioMedia.adjustRxLevel(2.0.toFloat())
                    audioMedia.adjustTxLevel(2.0.toFloat())
                } catch (_: Exception) {
                }
                audioMedia.startTransmit(audDevManager.playbackDevMedia)
                audDevManager.captureDevMedia.startTransmit(audioMedia)
            }
            sipService.sipEngine.am!!.mode = AudioManager.MODE_IN_COMMUNICATION
            setSpeaker(true)
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to start audio", e)
        }
    }

    fun stop() {
        try {
            val adm = sipService.sipEngine.endpoint?.audDevManager()
            val captureMedia = adm?.captureDevMedia
            val playbackMedia = adm?.playbackDevMedia
            val portId = audioMedia?.portId ?: -1
            if (audioMedia != null && portId >= 0) {
                try {
                    if (adm != null) {
                        captureMedia?.stopTransmit(audioMedia)
                    }
                    audioMedia?.stopTransmit(playbackMedia)
                } catch (e: Exception) {
                    Log.e("SipAudio", "Error while stopping transmit", e)
                }
            } else {
                Log.w("SipAudio", "audioMedia is null or has invalid portId ($portId)")
            }
            audioMedia = null
            sipService.sipEngine.am!!.mode = AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to stop audio", e)
        }
    }

    fun mute() {
        if (audioMedia == null) return
        try {
            isMute = true
            audioMedia!!.adjustTxLevel(0.0f)
            val capture = sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia
            capture?.adjustTxLevel(0.0f)
            capture?.stopTransmit(audioMedia)
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to mute audio", e)
        }
    }

    fun mic() {
        if (audioMedia == null) return
        try {
            isMute = false
            audioMedia!!.adjustTxLevel(2.0f)
            val capture = sipService.sipEngine.endpoint?.audDevManager()?.captureDevMedia
            capture?.adjustTxLevel(2.0f)
            capture?.startTransmit(audioMedia)
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to enable mic", e)
        }
    }

    fun setSpeaker(isLoudSpeaker: Boolean) {
        try {
            val adm: AudDevManager = sipService.sipEngine.endpoint!!.audDevManager()
            if (isLoudSpeaker) {
                adm.outputRoute = pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_LOUDSPEAKER
            } else {
                adm.outputRoute = pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_EARPIECE
            }
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
            } else {
                @Suppress("DEPRECATION")
                sipService.sipEngine.am!!.setSpeakerphoneOn(isLoudSpeaker)
            }
        } catch (_: Exception) {
        }
    }

    fun reconfigureAudioDevices(captureId: Int = 0, playbackId: Int = 0) {
        try {
            val adm = sipService.sipEngine.endpoint?.audDevManager()
            adm?.setCaptureDev(captureId)
            adm?.setPlaybackDev(playbackId)
        } catch (e: Exception) {
            Log.e("SipAudio", "Failed to reconfigure audio devices", e)
        }
    }
}
