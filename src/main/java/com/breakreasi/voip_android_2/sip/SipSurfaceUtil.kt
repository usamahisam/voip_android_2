package com.breakreasi.voip_android_2.sip

import android.view.SurfaceView

class SipSurfaceUtil {
    companion object {
        fun resizeFixWidth(surfaceView: SurfaceView, videoWidth: Int, videoHeight: Int) {
            val fixedWidth = surfaceView.getContext().getResources().getDisplayMetrics().widthPixels
            surfaceView.post(Runnable {
                val aspectRatio = videoHeight.toFloat() / videoWidth // height per 1 width
                val adjustedHeight = (fixedWidth * aspectRatio).toInt()
                val lp = surfaceView.getLayoutParams()
                lp.width = fixedWidth
                lp.height = adjustedHeight
                surfaceView.setLayoutParams(lp)
                surfaceView.invalidate()
                surfaceView.requestLayout()
            })
        }

        fun resizeFixHeight(surfaceView: SurfaceView, videoWidth: Int, videoHeight: Int) {
            val fixedHeight = surfaceView.getContext().getResources().getDisplayMetrics().heightPixels
            surfaceView.post(Runnable {
                val aspectRatio = videoWidth.toFloat() / videoHeight // width per 1 height
                val adjustedWidth = (fixedHeight * aspectRatio).toInt()
                val lp = surfaceView.getLayoutParams()
                lp.width = adjustedWidth
                lp.height = fixedHeight
                surfaceView.setLayoutParams(lp)
                surfaceView.invalidate()
                surfaceView.requestLayout()
            })
        }

        fun surfaceToTop(surfaceView: SurfaceView) {
            surfaceView.post(Runnable {
                surfaceView.setZOrderOnTop(true)
                surfaceView.setZOrderMediaOverlay(true)
                surfaceView.bringToFront()
                surfaceView.invalidate()
                surfaceView.requestLayout()
            })
        }

        fun surfaceToBottom(surfaceView: SurfaceView) {
            surfaceView.post(Runnable {
                surfaceView.setZOrderOnTop(false)
                surfaceView.setZOrderMediaOverlay(false)
                surfaceView.invalidate()
                surfaceView.requestLayout()
            })
        }
    }
}