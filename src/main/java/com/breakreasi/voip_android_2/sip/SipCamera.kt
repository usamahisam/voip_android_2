package com.breakreasi.voip_android_2.sip

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
        try {
            val devices = vidMgr.enumDev2()
            for (dev in devices) {
                if (dev.name.contains("camera")) {
                    if (dev.name.lowercase(Locale.getDefault()).contains("front")) {
                        frontCamera = dev.id
                    } else if (dev.name.lowercase(Locale.getDefault()).contains("back")) {
                        backCamera = dev.id
                    }
                }
            }
            switchCamera(isFrontCamera)
            setOrientation()
        } catch (_: Exception) {
        }
    }

    fun setOrientation() {
        try {
            if (isFrontCamera) {
                sipService.sipEngine.endpoint!!.vidDevManager().setCaptureOrient(frontCamera, pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG, true)
            } else {
                sipService.sipEngine.endpoint!!.vidDevManager().setCaptureOrient(backCamera, pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG, true)
            }
        } catch (_: Exception) {
        }
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