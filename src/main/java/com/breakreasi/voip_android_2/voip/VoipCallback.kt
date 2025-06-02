package com.breakreasi.voip_android_2.voip

interface VoipCallback {
    fun onAccountStatus(status: String): String
    fun onCallStatus(status: String): String
}