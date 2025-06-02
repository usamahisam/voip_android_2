package com.breakreasi.voip_android_2.voip

import android.content.Context

class Voip(
    private val context: Context,
) {
    private val voipServiceConnection = VoipServiceConnection(context)
    private val voipCallbacks = mutableListOf<VoipCallback>()

    fun engine(): VoipServiceConnection {
        return voipServiceConnection
    }

    fun engineStart() {
        voipServiceConnection.start()
    }

    fun registerCallback(callback: VoipCallback) {
        voipCallbacks.add(callback)
    }

    fun unregisterCallback(callback: VoipCallback) {
        voipCallbacks.remove(callback)
    }

    fun notifyAccountStatus(status: String) {
        voipCallbacks.forEach { it.onAccountStatus(status) }
    }

    fun notifyStatus(status: String) {
        voipCallbacks.forEach { it.onCallStatus(status) }
    }

    fun destroy() {
        voipServiceConnection.stop()
    }
}