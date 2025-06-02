package com.breakreasi.voip_android_2.sip

import org.pjsip.pjsua2.Account
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.AuthCredInfoVector
import org.pjsip.pjsua2.OnIncomingCallParam
import org.pjsip.pjsua2.OnRegStartedParam
import org.pjsip.pjsua2.OnRegStateParam
import org.pjsip.pjsua2.pj_constants_
import org.pjsip.pjsua2.pjmedia_srtp_use
import org.pjsip.pjsua2.pjsip_status_code

class SipAccount(
    private var sipService: SipService
): Account() {
    var accCfg: AccountConfig ?= null
    var host: String ?= null
    var port: Int ?= null
    var call: SipCall ?= null
    var destination: String = ""
    var withVideo: Boolean = false

    override fun onRegStarted(prm: OnRegStartedParam?) {
    }

    override fun onRegState(prm: OnRegStateParam?) {
        val status = checkAccountStatus()
        sipService.notifyAccountStatus(status)
        if (prm?.code == pjsip_status_code.PJSIP_SC_OK) {
            println("Registration successful: $status")
        } else {
            println("Registration failed: $status")
        }
    }

    override fun onIncomingCall(prm: OnIncomingCallParam?) {
        call = SipCall(sipService, this, prm?.callId).apply {
            makeRinging()
        }
    }

    fun auth(host: String?, port: Int?, displayName: String, username: String, password: String) {
        auth(host, port, displayName, username, password, "", false)
    }

    fun auth(host: String?, port: Int?, displayName: String, username: String, password: String, destination: String, withVideo: Boolean) {
        this.destination = destination
        this.withVideo = withVideo
        if (checkIsCreated()) {
            if (this.destination.isNotEmpty()) {
                newCall().makeCall(this.destination, withVideo)
            }
            return
        }
        val credArray = AuthCredInfoVector()
        val cred = AuthCredInfo("digest", "*", username, 0, password)
        credArray.add(cred)
        this.host = host
        this.port = port
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
            natConfig.sipStunUse = pj_constants_.PJ_FALSE
            natConfig.mediaStunUse = pj_constants_.PJ_FALSE
            mediaConfig.srtpUse = pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL
            mediaConfig.srtpSecureSignaling = 0
            mediaConfig.rtcpMuxEnabled = true
            mediaConfig.streamKaEnabled = true
            callConfig.timerSessExpiresSec = 300
            callConfig.timerMinSESec = 90
        }
    }

    fun createAccount(): Boolean {
        if (checkIsCreated()) return true
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

    private fun checkAccountStatus(): String {
        try {
            val info = this.info
            handleAccountSuccess()
            return "Account ID: ${info.id}, Reg Status: ${info.regStatusText}"
        } catch (e: Exception) {
            return "Account not created or failed: ${e.message}"
        }
    }

    private fun handleAccountSuccess() {
        if (this.destination.isNotEmpty()) {
            newCall().makeCall(destination, withVideo)
        }
    }

    fun newCall(): SipCall {
        call = SipCall(sipService,this)
        return call as SipCall
    }

    fun logout() {
        if (call != null) {
            try {
                call?.delete()
            } catch (_: Exception) {
            }
        }
        try {
            delete()
        } catch (_: Exception) {
        }
    }
}