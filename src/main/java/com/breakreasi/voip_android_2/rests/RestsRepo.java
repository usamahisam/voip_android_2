package com.breakreasi.voip_android_2.rests;

import com.breakreasi.voip_android_2.rests.ResponseInitConfigModel;
import com.breakreasi.voip_android_2.rests.ResponseModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RestsRepo {
    @POST("init_config")
    @FormUrlEncoded
    Call<ResponseInitConfigModel> initConfig(
            @Field("X-API-KEY") String API_KEY
    );

    @POST("user-receiver/accept_call")
    @FormUrlEncoded
    Call<ResponseModel> acceptCall(
            @Field("X-API-KEY") String API_KEY,
            @Field("token") String token,
            @Field("channel") String channel
    );

    @POST("user-receiver/reject_call")
    @FormUrlEncoded
    Call<ResponseModel> rejectCall(
            @Field("X-API-KEY") String API_KEY,
            @Field("token") String token
    );

    @POST("user-receiver/call")
    @FormUrlEncoded
    Call<ReceiverResponse> makeCall(
            @Field("X-API-KEY") String API_KEY,
            @Field("whatsapp") String whatsapp,
            @Field("lokasi") String lokasi,
            @Field("token") String token,
            @Field("with_video") boolean withVideo,
            @Field("type") String type
    );

}
