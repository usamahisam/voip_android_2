package com.breakreasi.voip_android_2.sip

import android.os.Build
import org.pjsip.pjsua2.CallVidSetStreamParam
import org.pjsip.pjsua2.pjmedia_orient
import org.pjsip.pjsua2.pjsua_call_vid_strm_op
import java.util.Locale

class SipCamera(
    private val sipService: SipService
) {
    private var isFrontCamera = true
    var frontCamera = 1
    var backCamera = 1

    fun configure() {
        val vidMgr = sipService.sipEngine.endpoint!!.vidDevManager()
        var frontFound = false
        var backFound = false

        try {
            val devices = vidMgr.enumDev2()
            for (dev in devices) {
                val name = dev.name.lowercase(Locale.getDefault())
                if (name.contains("camera")) {
                    if (!frontFound && name.contains("front")) {
                        frontCamera = dev.id
                        frontFound = true
                    } else if (!backFound && name.contains("back")) {
                        backCamera = dev.id
                        backFound = true
                    }
                }
            }
            for (dev in devices) {
                val name = dev.name.lowercase(Locale.getDefault())
                if (name.contains("camera")) {
                    if (!frontFound) {
                        frontCamera = dev.id
                        frontFound = true
                    } else if (!backFound && dev.id != frontCamera) {
                        backCamera = dev.id
                        backFound = true
                    }
                }
            }
            switchCamera(isFrontCamera)
            setOrientation()
        } catch (_: Exception) {
        }
    }

    fun setOrientation() {
        val orient = if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
            pjmedia_orient.PJMEDIA_ORIENT_ROTATE_90DEG
        } else {
            pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG
        }
        val camId = if (isFrontCamera) frontCamera else backCamera
        sipService.sipEngine.endpoint!!.vidDevManager().setCaptureOrient(camId, orient, true)
    }


    fun switchCamera(isFrontCamera: Boolean) {
        this.isFrontCamera = isFrontCamera
        val param = CallVidSetStreamParam()
        if (isFrontCamera) {
            param.capDev = frontCamera
        } else {
            param.capDev = backCamera
        }
        try {
            sipService.sipAccount!!.call!!.vidSetStream(pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_CHANGE_CAP_DEV, param)
        } catch (_: Exception) {
        }
    }

    fun switchCamera() {
        isFrontCamera = !isFrontCamera
        switchCamera(isFrontCamera)
    }
}