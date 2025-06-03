package com.breakreasi.voip_android_2.sip

import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    fun getClient(baseUrl: String): Retrofit {
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
}