package com.breakreasi.voip_android_2.agora

interface AgoraIEventListener {
    fun onAgoraStatus(status: String?)

    fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int)

    fun onRejoinChannelSuccess(channel: String?, uid: Int, elapsed: Int)

    fun onUserJoined(uid: Int, elapsed: Int)

    fun onUserOffline(uid: Int, reason: Int)

    fun onConnectionStateChanged(status: Int, reason: Int)

    fun onPeersOnlineStatusChanged(map: Map<String?, Int?>?)

    fun onError(err: Int)
}