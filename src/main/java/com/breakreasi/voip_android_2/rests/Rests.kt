package com.breakreasi.voip_android_2.rests

import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.MalformedURLException
import java.net.URL

class Rests {
    private var retrofit: Retrofit? = null
    private val repose: RestsRepo
    private val API_KEY = "1234"

    init {
        val client = getClient("https://api-receiver.jasvicall.my.id/")
        repose = client.create<RestsRepo>(RestsRepo::class.java)
    }

    fun getClient(baseUrl: String?): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        var myURL: URL? = null
        try {
            myURL = URL(baseUrl)
        } catch (ignored: MalformedURLException) {
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
                if (retrofit!!.baseUrl() != myURL) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(myURL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
                }
            }
        }
        return retrofit!!
    }

    fun initConfig(callback: ResponseCallback<ResponseInitConfigModel?>) {
        repose.initConfig(API_KEY)!!.enqueue(object : Callback<ResponseInitConfigModel?> {
            override fun onResponse(
                call: Call<ResponseInitConfigModel?>,
                response: Response<ResponseInitConfigModel?>
            ) {
                if (response.body() != null && (response.code() == 201 || response.code() == 200)) {
                    callback.onResponse(response.body())
                } else {
                    callback.onResponse(null)
                }
            }

            override fun onFailure(call: Call<ResponseInitConfigModel?>, t: Throwable) {
                callback.onResponse(null)
            }
        })
    }

    fun acceptCall(token: String?, channel: String?, callback: ResponseCallback<ResponseModel?>) {
        repose.acceptCall(API_KEY, token, channel)!!.enqueue(object : Callback<ResponseModel?> {
            override fun onResponse(
                call: Call<ResponseModel?>,
                response: Response<ResponseModel?>
            ) {
                if (response.body() != null && (response.code() == 201 || response.code() == 200)) {
                    callback.onResponse(response.body())
                } else {
                    callback.onResponse(null)
                }
            }

            override fun onFailure(call: Call<ResponseModel?>, t: Throwable) {
                callback.onResponse(null)
            }
        })
    }

    fun rejectCall(token: String?, callback: ResponseCallback<ResponseModel?>) {
        repose.rejectCall(API_KEY, token)!!.enqueue(object : Callback<ResponseModel?> {
            override fun onResponse(
                call: Call<ResponseModel?>,
                response: Response<ResponseModel?>
            ) {
                if (response.body() != null && (response.code() == 201 || response.code() == 200)) {
                    callback.onResponse(response.body())
                } else {
                    callback.onResponse(null)
                }
            }

            override fun onFailure(call: Call<ResponseModel?>, t: Throwable) {
                callback.onResponse(null)
            }
        })
    }

    fun makeCall(
        whatsapp: String?,
        lokasi: String?,
        token: String?,
        withVideo: Boolean,
        type: String?,
        callback: ResponseCallback<ReceiverResponse?>
    ) {
        repose.makeCall(API_KEY, whatsapp, lokasi, token, withVideo, type)!!
            .enqueue(object : Callback<ReceiverResponse?> {
                override fun onResponse(
                    call: Call<ReceiverResponse?>,
                    response: Response<ReceiverResponse?>
                ) {
                    if (response.body() != null && (response.code() == 201 || response.code() == 200)) {
                        callback.onResponse(response.body())
                    } else {
                        callback.onResponse(null)
                    }
                }

                override fun onFailure(call: Call<ReceiverResponse?>, t: Throwable) {
                    callback.onResponse(null)
                }
            })
    }
}
