package com.breakreasi.voip_android_2.rests

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RestsRepo {
    @POST("init_config")
    @FormUrlEncoded
    fun initConfig(
        @Field("X-API-KEY") API_KEY: String?
    ): Call<ResponseInitConfigModel?>?

    @POST("user-receiver/accept_call")
    @FormUrlEncoded
    fun acceptCall(
        @Field("X-API-KEY") API_KEY: String?,
        @Field("token") token: String?,
        @Field("channel") channel: String?
    ): Call<ResponseModel?>?

    @POST("user-receiver/reject_call")
    @FormUrlEncoded
    fun rejectCall(
        @Field("X-API-KEY") API_KEY: String?,
        @Field("token") token: String?
    ): Call<ResponseModel?>?

    @POST("user-receiver/call")
    @FormUrlEncoded
    fun makeCall(
        @Field("X-API-KEY") API_KEY: String?,
        @Field("whatsapp") whatsapp: String?,
        @Field("lokasi") lokasi: String?,
        @Field("token") token: String?,
        @Field("with_video") withVideo: Boolean,
        @Field("type") type: String?
    ): Call<ReceiverResponse?>?
}
