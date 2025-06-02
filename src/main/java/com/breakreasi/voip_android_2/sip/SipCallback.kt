package com.breakreasi.voip_android_2.sip

interface SipCallback {
    fun onAccountStatus(status: String): String
    fun onCallStatus(status: String): String
}