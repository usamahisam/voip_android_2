package com.breakreasi.voip_android_2.rests

interface ResponseCallback<T> {
    fun onResponse(response: T?)
}
