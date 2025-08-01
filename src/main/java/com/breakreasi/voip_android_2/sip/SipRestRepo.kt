package com.breakreasi.voip_android_2.sip

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SipRestRepo {
    @POST("api/check_auth.php")
    @FormUrlEncoded
    fun register(
        @Field("token") token: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("reg") reg: String
    ): Call<SipRestResponse?>?

    @POST("api/get_user.php")
    @FormUrlEncoded
    fun getUser(
        @Field("token") token: String,
        @Field("username") username: String,
    ): Call<SipRestResponse?>?

    @Multipart
    @POST("api/send_voicemail.php")
    fun sendVoicemail(
        @Part("token") token: RequestBody,
        @Part voicemailFile: MultipartBody.Part?,
        @Part("destination") destination: RequestBody,
        @Part("from") from: RequestBody
    ): Call<SipRestResponse?>?
}