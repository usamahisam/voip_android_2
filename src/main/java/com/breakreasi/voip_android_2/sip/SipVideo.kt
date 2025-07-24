package com.breakreasi.voip_android_2.sip

import android.util.Log
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
    private var w: Int = 480
    private var h: Int = 640
    private var toggleRemoteSurfaceFix = false

    fun configure() {
        try {
            val codecs: CodecInfoVector2 = sipService.sipEngine.endpoint!!.videoCodecEnum2()
            for (codec in codecs) {
                val codecId = codec.codecId
                val priority = when {
                    codecId.contains("H264/97") -> 128.toShort()
                    codecId.contains("H264/99") -> 127.toShort()
                    codecId.contains("VP8")     -> 93.toShort()
                    codecId.contains("VP9")     -> 80.toShort()
                    else -> 0.toShort()
                }
                sipService.sipEngine.endpoint!!.videoCodecSetPriority(codecId, priority)
                setCodecParam(codecId, sipService.sipEngine.endpoint!!.getVideoCodecParam(codecId))
            }
        } catch (e: Exception) {
            Log.e("SipVideo", "Failed to configure codecs: ${e.message}")
        }
        stop()
    }

    private fun setCodecParam(codecId: String, param: VidCodecParam) {
        val mediaFormatVideo = param.encFmt
        mediaFormatVideo.width = w.toLong()
        mediaFormatVideo.height = h.toLong()
        param.encFmt = mediaFormatVideo
        val codecFmtpVector = param.decFmtp
        for (i in codecFmtpVector.indices) {
            if (codecFmtpVector[i].name == "profile-level-id") {
                codecFmtpVector[i].setVal("42e01f")
                break
            }
        }
        param.decFmtp = codecFmtpVector
        try {
            sipService.sipEngine.endpoint!!.setVideoCodecParam(codecId, param)
        } catch (e: Exception) {
            Log.e("SipVideo", "Failed to set codec param: ${e.message}")
        }
    }

    fun start(mediaInfo: CallMediaInfo) {
        remoteVideo?.delete()
        localVideo?.delete()
        try {
            localVideo = VideoPreview(sipService.sipCamera.frontCamera)
            remoteVideo = VideoWindow(mediaInfo.videoIncomingWindowId)
            localVideoHandler.setPreview(localVideo!!)
            remoteVideoHandler.setVideo(remoteVideo!!)
            localVideoSurfaces.forEach {
                it.holder.addCallback(localVideoHandler)
                SipSurfaceUtil.surfaceToTop(it)
            }
            remoteVideoSurfaces.forEach {
                it.holder.addCallback(remoteVideoHandler)
                SipSurfaceUtil.surfaceToBottom(it)
            }
            toggleRemoteSurfaceFix = false
            toggleSurfaceRemoteFit()
        } catch (_: Exception) {
        }
    }

    fun changeFmt(w: Int, h: Int) {
//        this.w = w
//        this.h = h
//        toggleSurfaceRemoteFit()
    }

    fun stop() {
        clearLocalVideo()
        clearRemoteVideo()
    }

    fun addLocalVideoSurface(surfaceView: SurfaceView) {
        localVideoSurfaces.add(surfaceView)
    }

    fun addRemoteVideoSurface(surfaceView: SurfaceView) {
        remoteVideoSurfaces.add(surfaceView)
    }

    private fun clearLocalVideo() {
        localVideoSurfaces.clear()
    }

    private fun clearRemoteVideo() {
        remoteVideoSurfaces.clear()
    }

    fun toggleSurfaceRemoteFit() {
        if (toggleRemoteSurfaceFix) {
            remoteVideoSurfaces.forEach {
                SipSurfaceUtil.resizeFixWidth(it, w, h)
            }
        } else {
            remoteVideoSurfaces.forEach {
                SipSurfaceUtil.resizeFixHeight(it, w, h)
            }
        }
        toggleRemoteSurfaceFix = !toggleRemoteSurfaceFix
    }

    fun destroy() {
        stop()
    }
}
