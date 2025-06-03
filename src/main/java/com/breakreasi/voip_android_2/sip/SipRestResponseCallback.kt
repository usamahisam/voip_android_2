package com.breakreasi.voip_android_2.sip

interface SipRestResponseCallback<T> {
    fun onResponse(response: T?)
}