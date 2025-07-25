package com.breakreasi.voip_android_2.sip

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.MalformedURLException
import java.net.URL

class SipRest(
    private val sipService: SipService
) {
    private var retrofit: Retrofit? = null
    private var repo: SipRestRepo? = null
    private var token: String = "b98a6f78dsf63b23br09eb98fawbn9fds89"

    init {
        retrofit = getClient("http://" + sipService.host + "/")
        repo = retrofit!!.create(SipRestRepo::class.java)
    }

    private fun getClient(baseUrl: String): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        var myURL: URL? = null
        try {
            myURL = URL(baseUrl)
        } catch (_: MalformedURLException) {
        }
        if (retrofit == null) {
            if (myURL != null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(myURL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
        } else {
            if (myURL != null) {
                if (retrofit!!.baseUrl().toUri() != myURL.toURI()) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(myURL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
                }
            }
        }
        return retrofit!!
    }

    fun register(username: String, password: String, callback: SipRestResponseCallback<SipRestResponse>) {
        repo?.register(token, username, password, "1")?.enqueue(object : Callback<SipRestResponse?> {
            override fun onResponse(
                call: Call<SipRestResponse?>,
                response: Response<SipRestResponse?>
            ) {
                if (response.body() != null && response.body()!!.status == "success") {
                    callback.onResponse(response.body())
                } else {
                    callback.onResponse(null)
                }
            }
            override fun onFailure(
                call: Call<SipRestResponse?>,
                t: Throwable
            ) {
                callback.onResponse(null)
            }
        })
    }

    fun getUser(username: String, callback: SipRestResponseCallback<SipRestResponse>) {
        repo?.getUser(token, username)?.enqueue(object : Callback<SipRestResponse?> {
            override fun onResponse(
                call: Call<SipRestResponse?>,
                response: Response<SipRestResponse?>
            ) {
                if (response.body() != null && response.body()!!.status == "success") {
                    callback.onResponse(response.body())
                } else {
                    callback.onResponse(null)
                }
            }
            override fun onFailure(
                call: Call<SipRestResponse?>,
                t: Throwable
            ) {
                callback.onResponse(null)
            }
        })
    }

    fun sendVoicemail(file: File, destination: String, from: String, callback: SipRestResponseCallback<SipRestResponse>) {
        if (!file.exists()) {
            callback.onResponse(null)
            return
        }
        val textPlain = "text/plain".toMediaType()
        val audioAmr = "audio/amr".toMediaType()
        val requestToken: RequestBody = token.toRequestBody(textPlain)
        val requestDestination: RequestBody = destination.toRequestBody(textPlain)
        val requestFrom: RequestBody = from.toRequestBody(textPlain)
        val requestFile: RequestBody = file.asRequestBody(audioAmr)
        val voicemailPart: MultipartBody.Part = MultipartBody.Part.createFormData("voicemail", file.name, requestFile)
        repo?.sendVoicemail(requestToken, voicemailPart, requestDestination, requestFrom)?.enqueue(object : Callback<SipRestResponse?> {
            override fun onResponse(
                call: Call<SipRestResponse?>,
                response: Response<SipRestResponse?>
            ) {
                if (response.body() != null && response.body()!!.status == "success") {
                    callback.onResponse(response.body())
                } else {
                    callback.onResponse(null)
                }
            }
            override fun onFailure(
                call: Call<SipRestResponse?>,
                t: Throwable
            ) {
                callback.onResponse(null)
            }
        })
    }
}