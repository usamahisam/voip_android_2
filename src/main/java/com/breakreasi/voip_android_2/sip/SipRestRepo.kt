package com.breakreasi.voip_android_2.sip

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SipRestRepo {
    @POST("api/check_auth.php")
    @FormUrlEncoded
    fun register(
        @Field("token") token: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("reg") reg: String
    ): Call<SipRestResponse?>?
}