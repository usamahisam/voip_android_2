package com.breakreasi.voip_android_2.agora

import io.agora.rtc2.IRtcEngineEventHandler

class AgoraEngineEventListener : IRtcEngineEventHandler() {
    private val mListeners: MutableList<AgoraIEventListener> = ArrayList<AgoraIEventListener>()

    fun registerEventListener(listener: AgoraIEventListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }

    fun removeEventListener(listener: AgoraIEventListener) {
        mListeners.remove(listener)
    }

    fun onAgoraStatus(status: String?) {
        for (listener in mListeners) {
            listener.onAgoraStatus(status)
        }
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        val size = mListeners.size
        if (size > 0) {
            mListeners[size - 1].onJoinChannelSuccess(channel, uid, elapsed)
        }
    }

    override fun onRejoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        val size = mListeners.size
        if (size > 0) {
            mListeners[size - 1].onRejoinChannelSuccess(channel, uid, elapsed)
        }
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        val size = mListeners.size
        if (size > 0) {
            mListeners[size - 1].onUserJoined(uid, elapsed)
        }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        val size = mListeners.size
        if (size > 0) {
            mListeners[size - 1].onUserOffline(uid, reason)
        }
    }

    override fun onConnectionStateChanged(status: Int, reason: Int) {
        val size = mListeners.size
        if (size > 0) {
            mListeners[size - 1].onConnectionStateChanged(status, reason)
        }
    }
}