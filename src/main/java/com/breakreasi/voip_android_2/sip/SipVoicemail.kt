package com.breakreasi.voip_android_2.sip

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.breakreasi.voip_android_2.database.VoicemailModel
import com.breakreasi.voip_android_2.database.VoicemailPreferences
import java.io.File


class SipVoicemail(
    private val sipService: SipService
) {
    private var from: String = ""
    private var destination: String = ""
    private var recorder: MediaRecorder? = null
    private var outputFile: File ?= null
    val callTimeoutMillis = 30_000L
    val handler = Handler(Looper.getMainLooper())
    var countDownTimer: CountDownTimer? = null

    fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(sipService, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecord(from: String, destination: String) {
        this.from = from
        this.destination = destination
        this.outputFile = File(sipService.getExternalFilesDir(null), "Jasvicall Recordings/voicemail-${destination}.amr")
        if (outputFile!!.exists()) {
            outputFile!!.delete()
        }
        this.outputFile = File(sipService.getExternalFilesDir(null), "Jasvicall Recordings/voicemail-${destination}.amr")
        if (!checkAudioPermission()) {
            sipService.voip.notifyVoicemailRecord("failed")
            return
        }
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                setAudioEncodingBitRate(4750)
                setAudioSamplingRate(8000)
            } catch (_: Exception) {
            }
            setOutputFile(outputFile!!.absolutePath)
            prepare()
            start()
        }
        startTimer()
        sipService.voip.notifyVoicemailRecord("start")
    }

    fun stopRecord() {
        if (recorder == null) return
        cancelTimer()
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("SipVoicemail", "stopRecord: ", e)
        }
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
        sipService.sipRest.sendVoicemail(outputFile!!, destination, from, object : SipRestResponseCallback<SipRestResponse> {
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
        countDownTimer = object : CountDownTimer(callTimeoutMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000) + 1
                sipService.voip.notifyVoicemailTimer(secondsLeft)
            }
            override fun onFinish() {
                sipService.voip.notifyVoicemailRecord("timeout")
                stopRecord()
            }
        }.start()
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun getList(): MutableList<VoicemailModel>? {
        return VoicemailPreferences.getList(sipService)
    }
}