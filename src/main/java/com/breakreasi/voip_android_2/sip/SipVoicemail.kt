package com.breakreasi.voip_android_2.sip

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File


class SipVoicemail(
    private val sipService: SipService
) {
    private var destination: String = ""
    private var recorder: MediaRecorder? = null
    private var outputFile: File ?= null
    val callTimeoutMillis = 5_000L
    val handler = Handler(Looper.getMainLooper())
    var callTimeoutRunnable: Runnable? = null

    fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(sipService, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecord(destination: String) {
        this.destination = destination
        this.outputFile = File(sipService.filesDir, "voicemail-${destination}.record")
        if (outputFile!!.exists()) {
            outputFile!!.delete()
        }
        if (!checkAudioPermission()) {
            sipService.voip.notifyVoicemailRecord("failed")
            return
        }
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                setAudioEncodingBitRate(4750)
                setAudioSamplingRate(8000)
            } catch (_: Exception) {
            }
            setOutputFile(outputFile!!.absolutePath)
            prepare()
        }
        recorder?.start();
        startTimer()
        sipService.voip.notifyVoicemailRecord("start")
    }

    fun stopRecord() {
        if (recorder == null) return
        cancelTimer()
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        sipService.voip.notifyVoicemailRecord("stop")
        send()
    }

    fun send() {
        if (destination.isEmpty()) {
            sipService.voip.notifyVoicemailRecord("send_failed")
            return
        }
        if (outputFile == null) {
            sipService.voip.notifyVoicemailRecord("send_failed")
            return
        }
        sipService.sipRest.sendVoicemail(outputFile!!, destination, object : SipRestResponseCallback<SipRestResponse> {
            override fun onResponse(response: SipRestResponse?) {
                if (response != null && response.status == "success") {
                    sipService.voip.notifyVoicemailRecord("send_ok")
                    sipService.sipAccount!!.sendInstantMsg(destination, response.msg!!)
                } else {
                    sipService.voip.notifyVoicemailRecord("send_failed")
                }
            }
        })
    }

    fun startTimer() {
        cancelTimer()
        callTimeoutRunnable = Runnable {
            stopRecord()
        }
        handler.postDelayed(callTimeoutRunnable!!, callTimeoutMillis)
    }

    fun cancelTimer() {
        callTimeoutRunnable?.let {
            handler.removeCallbacks(it)
            callTimeoutRunnable = null
        }
    }
}