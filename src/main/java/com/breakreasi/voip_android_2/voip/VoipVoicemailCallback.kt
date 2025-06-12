package com.breakreasi.voip_android_2.voip

interface VoipVoicemailCallback {
    fun onVoicemailRecordStatus(status: String)
    fun onVoicemailRecordTimer(seconds: Long)
    fun onVoicemailReceive()
}