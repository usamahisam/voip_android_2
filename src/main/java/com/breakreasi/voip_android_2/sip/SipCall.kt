package com.breakreasi.voip_android_2.sip

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.breakreasi.voip_android_2.database.HistoryPreferences
import com.breakreasi.voip_android_2.voip.VoipType
import org.pjsip.pjsua2.*

class SipCall(
    private var sipService: SipService,
    private val account: SipAccount,
    callId: Int? = null
): Call(account, callId ?: -1) {
    val callTimeoutMillis = 30_000L
    val handler = Handler(Looper.getMainLooper())
    var callTimeoutRunnable: Runnable? = null
    var currentState: Int? = null
    var isCall: Boolean = false
    var audioMedia: Media? = null
    var withAudio: Boolean = false
    var withVideo: Boolean = false

    init {
        isCall = false
    }

    override fun onCallState(prm: OnCallStateParam?) {
        try {
            val callInfo = try { info } catch (e: Exception) {
                Log.e("SipCall", "Failed to get call info", e)
                return
            }

            if (currentState == pjsip_inv_state.PJSIP_INV_STATE_CALLING &&
                callInfo.lastStatusCode == 404) {
                isCall = false
                disconnected()
                return
            }

            Log.e("ABCDEF", ">> ${callInfo.state} | ${callInfo.lastStatusCode}")

            when (callInfo.state) {
                pjsip_inv_state.PJSIP_INV_STATE_INCOMING -> {
                    isCall = false
                    sendRinging()
                    withVideo = callInfo.remVideoCount > 0
                    sipService.voip.withVideo = withVideo
                    sipService.voip.notificationCallService(VoipType.SIP, sipService.sipAccount!!.displayName!!, withVideo, "")
                    sipService.voip.notifyCallStatus("incoming")
                }
                pjsip_inv_state.PJSIP_INV_STATE_CALLING -> {
                    isCall = false
                    sipService.voip.notifyCallStatus("calling")
                }
                pjsip_inv_state.PJSIP_INV_STATE_EARLY -> {
                    isCall = false
//                    sipService.voip.tone.start()
                    sipService.voip.notifyCallStatus("ringing")
                }
                pjsip_inv_state.PJSIP_INV_STATE_CONNECTING -> {
                    isCall = false
                    sipService.voip.notifyCallStatus("connecting")
                }
                pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
                    isCall = true
                    HistoryPreferences.save(sipService, sipService.voip.displayName, "Call Done")
                    sipService.voip.notifyCallStatus("connected")
                    cancelCallTimeout()
                }
                pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> {
                    isCall = false
//                    if (currentState != pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
//                    }
                    try {
//                        sipService.voip.tone.checkAndStop()
                        if (callInfo.lastStatusCode == pjsip_status_code.PJSIP_SC_REQUEST_TIMEOUT
                            || callInfo.lastStatusCode == pjsip_status_code.PJSIP_SC_REQUEST_TERMINATED) {
                            sipService.voip.handlerNotificationDecline(isMissed = true)
                        }
                    } catch (_: Exception) {
                    }
                    disconnected()
                }
                pjsip_inv_state.PJSIP_INV_STATE_NULL -> {}
            }
            currentState = callInfo.state
        } catch (e: Exception) {
            Log.e("SipCall", "onCallState failed", e)
        }
    }

    override fun onCallMediaState(prm: OnCallMediaStateParam?) {
        try {
            val callInfo = try { info } catch (e: Exception) {
                Log.e("SipCall", "Failed to get call info in onCallMediaState", e)
                return
            }
            audioMedia = null
            withAudio = false
            withVideo = false
            for (i in 0 until callInfo.media.size) {
                val media = getMedia(i.toLong())
                val mediaInfo = callInfo.media[i]
                if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                    media != null &&
                    mediaInfo.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                    audioMedia = media
                    withAudio = true
                    sipService.sipAudio.start(audioMedia!!)
                } else if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                    mediaInfo.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
                    mediaInfo.videoIncomingWindowId != pjsua2.INVALID_ID) {
                    sipService.sipVideo.start(mediaInfo)
                    withVideo = true
                }
            }
        } catch (e: Exception) {
            Log.e("SipCall", "onCallMediaState failed", e)
        }
    }

    override fun onCallMediaEvent(prm: OnCallMediaEventParam) {
        when (prm.ev.type) {
            pjmedia_event_type.PJMEDIA_EVENT_FMT_CHANGED -> {
                try {
                    val callInfo = try { info } catch (e: Exception) {
                        Log.e("SipCall", "Failed to get call info in onCallMediaEvent", e)
                        return
                    }

                    val mediaInfo = callInfo.media[prm.medIdx.toInt()]
                    if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                        mediaInfo.dir == pjmedia_dir.PJMEDIA_DIR_DECODING) {
                        val fmtEvent = prm.ev.data.fmtChanged
                        val w = fmtEvent.newWidth.toInt()
                        val h = fmtEvent.newHeight.toInt()
                        sipService.sipVideo.changeFmt(w, h)
                    }
                } catch (e: Exception) {
                    Log.e("SipCall", "FMT_CHANGED event handling failed", e)
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

    override fun onCallRxReinvite(prm: OnCallRxReinviteParam?) {
        try {
            val callOpParam = CallOpParam().apply {
                statusCode = pjsip_status_code.PJSIP_SC_OK
                opt.audioCount = if (withAudio) 1 else 0
                opt.videoCount = if (withVideo) 1 else 0
                opt.flag = pjsua_call_flag.PJSUA_CALL_INCLUDE_DISABLED_MEDIA.toLong()
            }
            answer(callOpParam)
        } catch (_: Exception) {
            try {
                val rejectParam = CallOpParam().apply {
                    statusCode = pjsip_status_code.PJSIP_SC_NOT_ACCEPTABLE_HERE
                }
                answer(rejectParam)
            } catch (_: Exception) {
            }
        }
    }

    fun startTimeoutCall() {
        cancelCallTimeout()
        callTimeoutRunnable = Runnable {
            sendTimeout()
        }
        handler.postDelayed(callTimeoutRunnable!!, callTimeoutMillis)
    }

    fun cancelCallTimeout() {
        if (callTimeoutRunnable == null) return
        callTimeoutRunnable?.let {
            handler.removeCallbacks(it)
            callTimeoutRunnable = null
        }
    }

    fun makeCall(user: String, withVideo: Boolean) {
        val sipUri = "sip:${user}@${account.host}:${account.port}"
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
            startTimeoutCall()
            account.isIncoming = false
        } catch (e: Exception) {
            Log.e("SipCall", "makeCall failed", e)
            cancelCallTimeout()
        }
    }

    fun sendRinging() {
        try {
            val callOpParam = CallOpParam().apply {
                statusCode = pjsip_status_code.PJSIP_SC_RINGING
            }
            answer(callOpParam)
        } catch (e: Exception) {
            Log.e("SipCall", "sendRinging failed", e)
        }
    }

    fun sendBusy() {
        try {
            val callOpParam = CallOpParam().apply {
                statusCode = pjsip_status_code.PJSIP_SC_BUSY_HERE
            }
            answer(callOpParam)
            disconnected()
        } catch (e: Exception) {
            Log.e("SipCall", "sendBusy failed", e)
        }
    }

    fun sendTimeout() {
        try {
            if (currentState == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED ||
                currentState == pjsip_inv_state.PJSIP_INV_STATE_NULL) {
                return
            }
            val callOpParam = CallOpParam().apply {
                statusCode = pjsip_status_code.PJSIP_SC_REQUEST_TIMEOUT
            }
            hangup(callOpParam)
        } catch (e: Exception) {
            Log.e("SipCall", "sendTimeout failed", e)
        }
        try {
            disconnected()
        } catch (_: Exception) {
        }
    }

    fun sendReinvite() {
        try {
            val callOpParam = CallOpParam().apply {
                opt.audioCount = if (withAudio) 1 else 0
                opt.videoCount = if (withVideo) 1 else 0
                opt.flag = 0
            }
            reinvite(callOpParam)
        } catch (_: Exception) {
        }
    }

    fun accept() {
        val callOpParam = CallOpParam().apply {
            statusCode = pjsip_status_code.PJSIP_SC_OK
            opt.audioCount = 1
            if (withVideo) {
                opt.videoCount = 1
            }
            opt.reqKeyframeMethod = pjsua_vid_req_keyframe_method.PJSUA_VID_REQ_KEYFRAME_RTCP_PLI.toLong()
        }
        try {
            answer(callOpParam)
        } catch (e: Exception) {
            Log.e("SipCall", "accept failed", e)
        }
    }

    fun decline() {
        if (currentState == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED ||
            currentState == pjsip_inv_state.PJSIP_INV_STATE_NULL) {
            return
        }
        val callOpParam = CallOpParam().apply {
            statusCode = pjsip_status_code.PJSIP_SC_DECLINE
        }
        try {
            hangup(callOpParam)
        } catch (e: Exception) {
            Log.e("SipCall", "decline failed", e)
        }
    }

    fun end() {
        if (currentState == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED ||
            currentState == pjsip_inv_state.PJSIP_INV_STATE_NULL) {
            return
        }
        val callOpParam = CallOpParam(true)
        try {
            hangup(callOpParam)
        } catch (e: Exception) {
            Log.e("SipCall", "decline failed", e)
        }
    }

    private fun sendKeyFrame() {
        try {
            vidSetStream(
                pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_SEND_KEYFRAME,
                CallVidSetStreamParam()
            )
        } catch (e: Exception) {
            Log.e("SipCall", "sendKeyFrame failed", e)
        }
    }

    private fun disconnected() {
        try {
            sipService.destroy()
        } catch (_: Exception) {
        }
//        try {
//            sipService.sipVideo.stop()
//        } catch (_: Exception) {
//        }
//        try {
//            sipService.sipAudio.stop()
//        } catch (_: Exception) {
//        }
//        try {
//            cancelCallTimeout()
//        } catch (_: Exception) {
//        }
//        try {
//            sipService.voip.notifyCallStatus("disconnected")
//        } catch (_: Exception) {
//        }
//        try {
//            destroyCall()
//        } catch (_: Exception) {
//        }
//        try {
//            sipService.deleteAccount()
//        } catch (_: Exception) {
//        }
//        try {
//            sipService.voip.stopNotificationCallService()
//        } catch (_: Exception) {
//        }
    }

//    fun destroyCall() {
//        try {
//            sipService.deleteCall()
//        } catch (e: Exception) {
//        }
//    }
}
