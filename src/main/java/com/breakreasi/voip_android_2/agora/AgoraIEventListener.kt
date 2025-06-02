package com.breakreasi.voip_android_2.agora

interface AgoraIEventListener {
    open fun onAgoraStatus(status: String?)

    open fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int)

    open fun onRejoinChannelSuccess(channel: String?, uid: Int, elapsed: Int)

    open fun onUserJoined(uid: Int, elapsed: Int)

    open fun onUserOffline(uid: Int, reason: Int)

    open fun onConnectionStateChanged(status: Int, reason: Int)

    open fun onPeersOnlineStatusChanged(map: Map<String?, Int?>?)

    open fun onError(err: Int)
}