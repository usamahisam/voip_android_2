package com.breakreasi.voip_android_2.sip

import android.view.SurfaceView
import org.pjsip.pjsua2.CallMediaInfo
import org.pjsip.pjsua2.CodecInfoVector2
import org.pjsip.pjsua2.VidCodecParam
import org.pjsip.pjsua2.VideoPreview
import org.pjsip.pjsua2.VideoWindow

class SipVideo(
    private val sipService: SipService
) {
    var remoteVideo: VideoWindow? = null
    var localVideo: VideoPreview? = null
    var localVideoHandler: SipSurfaceHandler = SipSurfaceHandler()
    var remoteVideoHandler: SipSurfaceHandler = SipSurfaceHandler()
    val localVideoSurfaces = mutableListOf<SurfaceView>()
    val remoteVideoSurfaces = mutableListOf<SurfaceView>()

    fun configure() {
        try {
            val codecs: CodecInfoVector2 = sipService.sipEngine.endpoint!!.videoCodecEnum2()
            for (codec in codecs) {
                val codecId = codec.codecId
                if (codecId.contains("H264/97")) {
                    sipService.sipEngine.endpoint!!.videoCodecSetPriority(codecId, 128.toShort())
                    setCodecParam(codecId, sipService.sipEngine.endpoint!!.getVideoCodecParam(codecId))
                } else if (codecId.contains("H264/99")) {
                    sipService.sipEngine.endpoint!!.videoCodecSetPriority(codecId, 127.toShort())
                    setCodecParam(codecId, sipService.sipEngine.endpoint!!.getVideoCodecParam(codecId))
                } else if (codecId.contains("VP8")) {
                    sipService.sipEngine.endpoint!!.videoCodecSetPriority(codecId, 93.toShort())
                    setCodecParam(codecId, sipService.sipEngine.endpoint!!.getVideoCodecParam(codecId))
                } else if (codecId.contains("VP9")) {
                    sipService.sipEngine.endpoint!!.videoCodecSetPriority(codecId, 80.toShort())
                    setCodecParam(codecId, sipService.sipEngine.endpoint!!.getVideoCodecParam(codecId))
                } else {
                    sipService.sipEngine.endpoint!!.videoCodecSetPriority(codecId, 0.toShort())
                    setCodecParam(codecId, sipService.sipEngine.endpoint!!.getVideoCodecParam(codecId))
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun setCodecParam(codecId: String, param: VidCodecParam) {
        val mediaFormatVideo = param.encFmt
        mediaFormatVideo.width = 480
        mediaFormatVideo.height = 640
        param.encFmt = mediaFormatVideo
        val codecFmtpVector = param.decFmtp
        for (i in codecFmtpVector.indices) {
            if ("profile-level-id" == codecFmtpVector[i].name) {
                codecFmtpVector[i].setVal("42e01f")
                break
            }
        }
        param.decFmtp = codecFmtpVector
        try {
            sipService.sipEngine.endpoint!!.setVideoCodecParam(codecId, param)
        } catch (_: Exception) {
        }
    }

    fun start(mediaInfo: CallMediaInfo) {
        remoteVideo?.delete()
        localVideo?.delete()
        localVideo = VideoPreview(sipService.sipCamera.frontCamera)
        remoteVideo = VideoWindow(mediaInfo.videoIncomingWindowId)
        startLocal()
        startRemote()
    }

    fun changeFmt(w: Int, h: Int) {
    }

    fun stop() {
        clearLocalVideo()
        clearRemoteVideo()
        try {
            remoteVideo?.delete()
            remoteVideo = null
        } catch (_: Exception) {
        }
        try {
            localVideo?.delete()
            localVideo = null
        } catch (_: Exception) {
        }
    }

    fun addLocalVideoSurface(surfaceView: SurfaceView) {
        surfaceView.holder.addCallback(localVideoHandler)
        localVideoSurfaces.add(surfaceView)
        startLocal()
    }

    fun addRemoteVideoSurface(surfaceView: SurfaceView) {
        surfaceView.holder.addCallback(remoteVideoHandler)
        remoteVideoSurfaces.add(surfaceView)
        startRemote()
    }

    private fun startLocal() {
        if (localVideo == null) return
        localVideoHandler.start(localVideo!!)
    }

    private fun startRemote() {
        if (remoteVideo == null) return
        remoteVideoHandler.start(remoteVideo!!)
    }

    private fun clearLocalVideo() {
        localVideoSurfaces.clear()
    }

    private fun clearRemoteVideo() {
        remoteVideoSurfaces.clear()
    }
}