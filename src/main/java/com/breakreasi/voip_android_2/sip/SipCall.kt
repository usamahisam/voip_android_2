package com.breakreasi.voip_android_2.sip

import android.util.Log
import com.breakreasi.voip_android_2.voip.VoipType
import org.pjsip.pjsua2.Call
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.CallVidSetStreamParam
import org.pjsip.pjsua2.OnCallMediaEventParam
import org.pjsip.pjsua2.OnCallMediaStateParam
import org.pjsip.pjsua2.OnCallStateParam
import org.pjsip.pjsua2.pjmedia_dir
import org.pjsip.pjsua2.pjmedia_event_type
import org.pjsip.pjsua2.pjmedia_rtcp_fb_type
import org.pjsip.pjsua2.pjmedia_type
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code
import org.pjsip.pjsua2.pjsua2
import org.pjsip.pjsua2.pjsua_call_flag
import org.pjsip.pjsua2.pjsua_call_media_status
import org.pjsip.pjsua2.pjsua_call_vid_strm_op
import org.pjsip.pjsua2.pjsua_vid_req_keyframe_method

class SipCall(
    private var sipService: SipService,
    private val account: SipAccount,
    callId: Int? = null
): Call(account, callId ?: -1) {
    var currentState: Int? = null
    var withVideo: Boolean = false

    override fun onCallState(prm: OnCallStateParam?) {
        try {
            when (info.state) {
                pjsip_inv_state.PJSIP_INV_STATE_INCOMING -> {
                    makeRinging()
                    withVideo = info.remVideoCount > 0
                    sipService.voip.withVideo = withVideo
                    sipService.voip.notificationCallService(VoipType.SIP, sipService.sipAccount.displayName!!, withVideo, "")
                    sipService.voip.notifyCallStatus("incoming")
                }
                pjsip_inv_state.PJSIP_INV_STATE_CALLING -> {
                    sipService.voip.notifyCallStatus("calling")
                }
                pjsip_inv_state.PJSIP_INV_STATE_EARLY -> {
                    sipService.voip.notifyCallStatus("ringing")
                }
                pjsip_inv_state.PJSIP_INV_STATE_CONNECTING -> {
                    sipService.voip.notifyCallStatus("connecting")
                }
                pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
                    sipService.voip.notifyCallStatus("connected")
                    sipService.sipAudio.setSpeaker(withVideo)
                }
                pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> {
                    if (currentState != pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
                        disconnected()
                    }
                }
                pjsip_inv_state.PJSIP_INV_STATE_NULL -> {}
            }
            currentState = info.state
        } catch (_: Exception) {
        }
    }

    override fun onCallMediaState(prm: OnCallMediaStateParam?) {
        try {
            val info = info
            withVideo = false
            for (i in 0 until info.media.size) {
                val media = getMedia(i.toLong())
                val mediaInfo = info.media[i]
                if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                    media != null &&
                    mediaInfo.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                    sipService.sipAudio.start(media)
                } else if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                    mediaInfo.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
                    mediaInfo.videoIncomingWindowId != pjsua2.INVALID_ID) {
                    sipService.sipVideo.start(mediaInfo)
                    withVideo = true
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onCallMediaEvent(prm: OnCallMediaEventParam) {
        when (prm.ev.type) {
            pjmedia_event_type.PJMEDIA_EVENT_FMT_CHANGED -> {
                try {
                    val callInfo = info
                    val mediaInfo = callInfo.media[prm.medIdx.toInt()]
                    if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                        mediaInfo.dir == pjmedia_dir.PJMEDIA_DIR_DECODING) {
                        val fmtEvent = prm.ev.data.fmtChanged
                        val w = fmtEvent.newWidth.toInt()
                        val h = fmtEvent.newHeight.toInt()
                        sipService.sipVideo.changeFmt(w, h)
                    }
                } catch (_: Exception) {
                }
            }
            pjmedia_event_type.PJMEDIA_EVENT_RX_RTCP_FB -> {
                val rtcpFb = prm.ev.data?.rtcpFb
                if (rtcpFb != null &&
                    rtcpFb.fbType == pjmedia_rtcp_fb_type.PJMEDIA_RTCP_FB_NACK &&
                    rtcpFb.isParamLengthZero
                ) {
                    sendKeyFrame()
                }
            }
        }
    }

    fun makeCall(user: String, withVideo: Boolean) {
        val sipUri = "sip:$user@${account.host}:${account.port}"
        val callOpParam = CallOpParam().apply {
            opt.audioCount = 1
            opt.videoCount = if (withVideo) 1 else 0
            if (!withVideo) {
                opt.flag = pjsua_call_flag.PJSUA_CALL_INCLUDE_DISABLED_MEDIA.toLong()
            }
            opt.reqKeyframeMethod = pjsua_vid_req_keyframe_method.PJSUA_VID_REQ_KEYFRAME_RTCP_PLI.toLong()
        }
        try {
            makeCall(sipUri, callOpParam)
        } catch (_: Exception) {
        }
    }

    fun makeRinging() {
        try {
            val callOpParam = CallOpParam().apply {
                statusCode = pjsip_status_code.PJSIP_SC_RINGING
            }
            answer(callOpParam)
        } catch (_: Exception) {
        }
    }

    fun accept() {
        val callOpParam = CallOpParam()
        callOpParam.statusCode = pjsip_status_code.PJSIP_SC_OK
        callOpParam.opt.audioCount = 1
        if (withVideo) {
            callOpParam.opt.videoCount = 1
        }
        callOpParam.opt.reqKeyframeMethod = pjsua_vid_req_keyframe_method.PJSUA_VID_REQ_KEYFRAME_RTCP_PLI.toLong()
        try {
            answer(callOpParam)
        } catch (_: Exception) {
        }
    }

    fun decline() {
        val callOpParam = CallOpParam()
        callOpParam.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
        try {
            hangup(callOpParam)
        } catch (_: Exception) {
        }
    }

    private fun sendKeyFrame() {
        try {
            vidSetStream(
                pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_SEND_KEYFRAME,
                CallVidSetStreamParam()
            )
        } catch (_: Exception) {
        }
    }

    private fun disconnected() {
        sipService.voip.stopNotificationCallService()
//        sipService.sipAudio.stop()
        sipService.sipVideo.stop()
        sipService.voip.notifyCallStatus("disconnected")
        account.delete()
    }
}