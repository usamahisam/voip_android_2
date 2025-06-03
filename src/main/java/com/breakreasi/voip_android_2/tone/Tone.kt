package com.breakreasi.voip_android_2.tone

import android.media.AudioManager
import android.media.ToneGenerator


class Tone {
    private var toneGenerator: ToneGenerator? = null

    fun start() {
        toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100)
        toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE)
    }

    fun checkAndStop() {
        if (toneGenerator != null){
            toneGenerator?.stopTone();
            toneGenerator?.release();
            toneGenerator = null;
        }
    }
}