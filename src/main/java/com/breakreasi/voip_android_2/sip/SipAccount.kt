package com.breakreasi.voip_android_2.sip

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.breakreasi.voip_android_2.database.VoicemailPreferences
import org.pjsip.pjsua2.*

class SipAccount(
    private var sipService: SipService
) : Account() {

    var accCfg: AccountConfig? = null
    var host: String? = null
    var port: Int? = null
    var displayName: String? = null
    var username: String? = null
    var password: String? = null
    var call: SipCall? = null
    var destination: String = ""
    var isIncoming: Boolean = false
    var withVideo: Boolean = false

    override fun onRegStarted(prm: OnRegStartedParam?) {
    }

    override fun onRegState(prm: OnRegStateParam?) {
        if (prm?.code == pjsip_status_code.PJSIP_SC_OK) {
            handleAccountSuccess()
        } else {
            sipService.deleteAccount()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onIncomingCall(prm: OnIncomingCallParam?) {
        try {
            if (call != null) {
                sipService.deleteCall()
            }
            call = SipCall(sipService, this, prm?.callId)
        } catch (e: Exception) {
            Log.e("SipAccount", "Error handling incoming call", e)
        }
    }

    override fun onInstantMessage(prm: OnInstantMessageParam?) {
        if (prm == null) return
        try {
            val message = prm.msgBody ?: return
            val voicemailRegex = Regex("""\[voicemail_url:(.+?)\]""")
            val fromRegex = Regex("""\[from:(.+?)\]""")

            val voicemailUrl = voicemailRegex.find(message)?.groupValues?.getOrNull(1)
            val fromNumber = fromRegex.find(message)?.groupValues?.getOrNull(1)

            Log.d("SipAccount", "Received IM from ${prm.fromUri}, msg=$message")

            if (!voicemailUrl.isNullOrBlank() && !fromNumber.isNullOrBlank()) {
                VoicemailPreferences.save(sipService, fromNumber, fromNumber, voicemailUrl)
                sipService.voip.notificationVoicemailService(fromNumber, voicemailUrl)
            } else {
                Log.w("SipAccount", "Voicemail message missing URL or from field")
            }
        } catch (e: Exception) {
            Log.e("SipAccount", "Error handling incoming message", e)
        }
    }

    fun auth(
        host: String,
        port: Int,
        displayName: String,
        username: String,
        password: String,
        destination: String,
        withVideo: Boolean
    ) {
        this.host = host
        this.port = port
        this.displayName = displayName
        this.username = username
        this.password = password
        this.destination = destination
        this.withVideo = withVideo
        this.isIncoming = this.destination.isNotEmpty()

        if (checkIsCreated()) {
            sipService.voip.notifyAccountStatus("success")
            if (isIncoming) {
                sipService.call(this.destination, withVideo)
            }
            return
        }

        sipService.sipRest.register(username, password, object : SipRestResponseCallback<SipRestResponse> {
            override fun onResponse(response: SipRestResponse?) {
                if (response != null && response.status == "success") {
                    val created = createAccount(displayName, response.msg!!, password, destination, withVideo)
                    if (!created) {
                        sipService.voip.notifyAccountStatus("failed")
                    }
                } else {
                    sipService.voip.notifyAccountStatus("failed")
                }
            }
        })
    }

    private fun createAccount(
        displayName: String,
        username: String,
        password: String,
        destination: String,
        withVideo: Boolean
    ): Boolean {
        return try {
            val credArray = AuthCredInfoVector()
            val cred = AuthCredInfo("digest", "*", username, 0, password)
            credArray.add(cred)

            accCfg = AccountConfig().apply {
                idUri = "\"$displayName\" <sip:$username@${host}:${port}>"
                sipConfig.apply {
                    authCreds = credArray
                    proxies.apply {
                        clear()
                        add("sip:$username@${host}:${port};lr;nat=yes")
                    }
                }
                regConfig.apply {
                    registrarUri = "sip:${host}:${port}"
                    registerOnAdd = true
                    dropCallsOnFail = true
                    contactUriParams = ";nat=yes"
                }
                natConfig.apply {
                    iceEnabled = false
                    turnEnabled = false
                    sdpNatRewriteUse = pj_constants_.PJ_TRUE
                    viaRewriteUse = pj_constants_.PJ_TRUE
                    sipStunUse = pj_constants_.PJ_FALSE
                    mediaStunUse = pj_constants_.PJ_FALSE
                }
                callConfig.apply {
                    timerSessExpiresSec = 300
                    timerMinSESec = 90
                    timerUse = pj_constants_.PJ_TRUE
                }
                mediaConfig.apply {
                    srtpUse = pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL
                    srtpSecureSignaling = 0
                    rtcpMuxEnabled = true
                    streamKaEnabled = true
                }
                videoConfig.apply {
                    defaultCaptureDevice = 1
                    defaultRenderDevice = 0
                    autoTransmitOutgoing = true
                    autoShowIncoming = true
                }
            }
            create(accCfg)
            true
        } catch (e: Exception) {
            Log.e("SipAccount", "Failed to create account", e)
            false
        }
    }

    private fun checkIsCreated(): Boolean {
        return id != -1
    }

    private fun handleAccountSuccess() {
        try {
            sipService.voip.notifyAccountStatus("success")
            if (this.isIncoming) {
                sipService.call(destination, withVideo)
            }
        } catch (e: Exception) {
            Log.e("SipAccount", "handleAccountSuccess exception", e)
            sipService.voip.notifyAccountStatus("blocked")
        }
    }

    fun newCall(): SipCall {
        if (call != null) {
            call!!.delete()
        }
        call = SipCall(sipService, this)
        return call!!
    }

    fun checkIsCall(): Boolean {
        return call?.isCall ?: false
    }

    fun sendInstantMsg(destination: String, msg: String) {
        try {
            val buddy = Buddy()
            val bCfg = BuddyConfig().apply {
                uri = "sip:$destination@${host}:${port}"
                subscribe = false
            }
            buddy.create(this, bCfg)

            val prm = SendInstantMessageParam().apply {
                contentType = "text/plain"
                content = msg
            }

            buddy.sendInstantMessage(prm)
            buddy.delete()
        } catch (e: Exception) {
            Log.e("SipAccount", "sendInstantMsg exception", e)
        }
    }
}
