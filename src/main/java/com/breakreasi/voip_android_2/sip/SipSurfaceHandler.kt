package com.breakreasi.voip_android_2.sip

import android.view.Surface
import android.view.SurfaceHolder
import org.pjsip.pjsua2.VideoPreview
import org.pjsip.pjsua2.VideoPreviewOpParam
import org.pjsip.pjsua2.VideoWindow
import org.pjsip.pjsua2.VideoWindowHandle

class SipSurfaceHandler: SurfaceHolder.Callback {
    private var isStart: Boolean = false
    private var surface: Surface ?= null

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = holder.surface
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isStart = false;
    }

    fun start(vp: VideoPreview) {
        if (isStart) return
        val videoWindowHandle = VideoWindowHandle()
        videoWindowHandle.handle.setWindow(surface)
        try {
            val videoPreviewOpParam = VideoPreviewOpParam()
            videoPreviewOpParam.window = videoWindowHandle
            vp.start(videoPreviewOpParam)
            isStart = true;
        } catch (_: Exception) {
        }
    }

    fun start(vw: VideoWindow) {
        if (isStart) return
        val videoWindowHandle = VideoWindowHandle()
        videoWindowHandle.handle.setWindow(surface)
        vw.setWindow(videoWindowHandle)
        try {
            val w = vw.info.getSize().w.toInt()
            val h = vw.info.getSize().h.toInt()
            isStart = true;
        } catch (_: Exception) {
        }
    }
}