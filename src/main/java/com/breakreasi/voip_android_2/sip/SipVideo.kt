package com.breakreasi.voip_android_2.sip

import android.view.SurfaceView
import org.pjsip.pjsua2.CallMediaInfo
import org.pjsip.pjsua2.VideoPreview
import org.pjsip.pjsua2.VideoPreviewOpParam
import org.pjsip.pjsua2.VideoWindow
import org.pjsip.pjsua2.VideoWindowHandle

class SipVideo {
    var remoteVideo: VideoWindow? = null
    var localVideo: VideoPreview? = null
    var localVideoHandler: SipSurfaceHandler = SipSurfaceHandler()
    var remoteVideoHandler: SipSurfaceHandler = SipSurfaceHandler()
    val localVideoSurfaces = mutableListOf<SurfaceView>()
    val remoteVideoSurfaces = mutableListOf<SurfaceView>()

    fun start(mediaInfo: CallMediaInfo) {
        remoteVideo?.delete()
        localVideo?.delete()
        localVideo = VideoPreview(0)
        remoteVideo = VideoWindow(mediaInfo.videoIncomingWindowId)
        startLocal()
        startRemote()
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