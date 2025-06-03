package com.breakreasi.voip_android_2.voip

interface VoipCallback {
    fun onAccountStatus(status: String)
    fun onCallStatus(voip: Voip, status: String)
}