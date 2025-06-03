package com.breakreasi.voip_android_2.rests;

import android.content.Context;

import androidx.annotation.NonNull;

import com.breakreasi.voip_android_2.rests.ResponseInitConfigModel;
import com.breakreasi.voip_android_2.rests.ResponseModel;
import com.breakreasi.voip_android_2.rests.RestsRepo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Rests {
    private Retrofit retrofit;
    private final RestsRepo repose;
    private final String API_KEY = "1234";

    public Rests() {
        Retrofit client = getClient("https://api-receiver.jasvicall.my.id/");
        repose = client.create(RestsRepo.class);
    }

    public Retrofit getClient(String baseUrl) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        URL myURL = null;
        try {
            myURL = new URL(baseUrl);
        } catch (MalformedURLException ignored) {
        }
        if (retrofit == null) {
            if (myURL != null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(myURL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
        } else {
            if (myURL != null) {
                if (!retrofit.baseUrl().equals(myURL)) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(myURL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofit;
    }

    public void initConfig(ResponseCallback<ResponseInitConfigModel> callback) {
        repose.initConfig(API_KEY).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseInitConfigModel> call, @NonNull Response<ResponseInitConfigModel> response) {
                if (response.body() != null && (response.code() == 201 || response.code() == 200)) {
                    callback.onResponse(response.body());
                } else {
                    callback.onResponse(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseInitConfigModel> call, @NonNull Throwable t) {
                callback.onResponse(null);
            }
        });
    }

    public void acceptCall(String token, String channel, ResponseCallback<ResponseModel> callback) {
        repose.acceptCall(API_KEY, token, channel).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull Response<ResponseModel> response) {
                if (response.body() != null && (response.code() == 201 || response.code() == 200)) {
                    callback.onResponse(response.body());
                } else {
                    callback.onResponse(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
                callback.onResponse(null);
            }
        });
    }

    public void rejectCall(String token, ResponseCallback<ResponseModel> callback) {
        repose.rejectCall(API_KEY, token).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull Response<ResponseModel> response) {
                if (response.body() != null && (response.code() == 201 || response.code() == 200)) {
                    callback.onResponse(response.body());
                } else {
                    callback.onResponse(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
                callback.onResponse(null);
            }
        });
    }

    public void makeCall(String whatsapp, String lokasi, String token, boolean withVideo, String type, ResponseCallback<ReceiverResponse> callback) {
        repose.makeCall(API_KEY, whatsapp, lokasi, token, withVideo, type).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ReceiverResponse> call, @NonNull Response<ReceiverResponse> response) {
                if (response.body() != null && (response.code() == 201 || response.code() == 200)) {
                    callback.onResponse(response.body());
                } else {
                    callback.onResponse(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ReceiverResponse> call, @NonNull Throwable t) {
                callback.onResponse(null);
            }
        });
    }
}
