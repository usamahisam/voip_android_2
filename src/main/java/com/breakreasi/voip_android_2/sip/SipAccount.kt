package com.breakreasi.voip_android_2.sip

import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import org.pjsip.pjsua2.Account
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.AuthCredInfoVector
import org.pjsip.pjsua2.Buddy
import org.pjsip.pjsua2.BuddyConfig
import org.pjsip.pjsua2.OnIncomingCallParam
import org.pjsip.pjsua2.OnInstantMessageParam
import org.pjsip.pjsua2.OnRegStartedParam
import org.pjsip.pjsua2.OnRegStateParam
import org.pjsip.pjsua2.SendInstantMessageParam
import org.pjsip.pjsua2.pj_constants_
import org.pjsip.pjsua2.pjmedia_srtp_use
import org.pjsip.pjsua2.pjsip_status_code
import java.io.File
import java.nio.file.Files


class SipAccount(
    private var sipService: SipService
): Account() {
    var accCfg: AccountConfig ?= null
    var host: String ?= null
    var port: Int ?= null
    var displayName: String ?= null
    var username: String ?= null
    var password: String ?= null
    var call: SipCall ?= null
    var destination: String = ""
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
        if (call != null && call!!.isActive) {
            call!!.delete()
        }
        call = SipCall(sipService, this, prm?.callId)
    }

    override fun onInstantMessage(prm: OnInstantMessageParam?) {
    }

    fun auth(host: String, port: Int, displayName: String, username: String, password: String, destination: String, withVideo: Boolean) {
        this.host = host
        this.port = port
        this.displayName = displayName
        this.username = username
        this.password = password
        this.destination = destination
        this.withVideo = withVideo
        if (checkIsCreated()) {
            sipService.voip.notifyAccountStatus("success")
            if (this.destination.isNotEmpty()) {
                newCall().makeCall(this.destination, withVideo)
            }
            return
        }
        sipService.sipRest.register(username, password, object : SipRestResponseCallback<SipRestResponse> {
            override fun onResponse(response: SipRestResponse?) {
                if (response != null && response.status == "success") {
                    createAccount(displayName, username, password, destination, withVideo)
                } else {
                    sipService.voip.notifyAccountStatus("failed")
                }
            }
        })
    }

    private fun createAccount(displayName: String, username: String, password: String, destination: String, withVideo: Boolean): Boolean {
        val credArray = AuthCredInfoVector()
        val cred = AuthCredInfo("digest", "*", username, 0, password)
        credArray.add(cred)
        accCfg = AccountConfig().apply {
            idUri = "\"$displayName\" <sip:$username@${host}:${port}>"
            sipConfig.authCreds = credArray
            sipConfig.proxies.clear()
            regConfig.registrarUri = "sip:${host}:${port}"
            regConfig.registerOnAdd = true
            regConfig.dropCallsOnFail = true
            videoConfig.defaultCaptureDevice = 1
            videoConfig.defaultRenderDevice = 0
            videoConfig.autoTransmitOutgoing = true
            videoConfig.autoShowIncoming = true
            natConfig.iceEnabled = false
            natConfig.turnEnabled = false
            natConfig.sdpNatRewriteUse = pj_constants_.PJ_FALSE
            natConfig.viaRewriteUse = pj_constants_.PJ_FALSE
            natConfig.sipStunUse = pj_constants_.PJ_FALSE
            natConfig.mediaStunUse = pj_constants_.PJ_FALSE
            mediaConfig.srtpUse = pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL
            mediaConfig.srtpSecureSignaling = 0
            mediaConfig.rtcpMuxEnabled = true
            mediaConfig.streamKaEnabled = true
            callConfig.timerSessExpiresSec = 300
            callConfig.timerMinSESec = 90
            callConfig.timerUse = pj_constants_.PJ_TRUE
        }
        return try {
            create(accCfg)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun checkIsCreated(): Boolean {
        return id != -1
    }

    private fun handleAccountSuccess() {
        try {
            sipService.voip.notifyAccountStatus("success")
            if (this.destination.isNotEmpty()) {
                newCall().makeCall(destination, withVideo)
            }
        } catch (_: Exception) {
            sipService.voip.notifyAccountStatus("blocked")
        }
    }

    fun newCall(): SipCall {
        if (call != null && call!!.isActive) {
            call!!.delete()
        }
        call = SipCall(sipService, this)
        return call as SipCall
    }

    fun checkIsCall(): Boolean {
        if (call == null) return false
        return call!!.isCall
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
        } catch (_: Exception) {
        }
    }
}