package com.breakreasi.voip_android_2.sip

import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import org.pjsip.pjsua2.VideoPreview
import org.pjsip.pjsua2.VideoPreviewOpParam
import org.pjsip.pjsua2.VideoWindow
import org.pjsip.pjsua2.VideoWindowHandle

class SipSurfaceHandler : SurfaceHolder.Callback {

    private var surface: Surface? = null
    private var isStarted = false

    private var preview: VideoPreview? = null
    private var window: VideoWindow? = null

    fun setPreview(vp: VideoPreview) {
        this.preview = vp
    }

    fun setVideo(vw: VideoWindow) {
        this.window = vw
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = holder.surface
        Log.d("SipSurfaceHandler", "Surface created")
        startIfReady()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Optional
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surface = null
        isStarted = false
        try {
            preview?.stop()
        } catch (_: Exception) {
        }
        try {
            window?.setWindow(null)
        } catch (_: Exception) {
        }
    }

    private fun startIfReady() {
        if (isStarted || surface == null) return

        try {
            val handle = VideoWindowHandle()
            handle.handle.setWindow(surface)

            if (preview != null) {
                val op = VideoPreviewOpParam()
                op.window = handle
                preview!!.start(op)
                Log.d("SipSurfaceHandler", "Preview started")
            } else if (window != null) {
                window!!.setWindow(handle)
                Log.d("SipSurfaceHandler", "Video window set")
            }
            isStarted = true
        } catch (e: Exception) {
            Log.e("SipSurfaceHandler", "startIfReady error: ${e.message}")
        }
    }
}
