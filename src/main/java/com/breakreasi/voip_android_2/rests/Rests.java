package com.breakreasi.voip_android_2.rests;

import android.content.Context;

import com.breakreasi.voip_android_2.rests.ResponseInitConfigModel;
import com.breakreasi.voip_android_2.rests.ResponseModel;
import com.breakreasi.voip_android_2.rests.RestsRepo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Rests {
    private Retrofit retrofit;
    private final RestsRepo repose;
    private final String API_KEY;

    public Rests(Context context) {
        String BASE_URL_API = "https://api-receiver.jasvicall.my.id/";
        API_KEY = "1234";
        Retrofit client = getClient(BASE_URL_API);
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

    public void initConfig(Callback<ResponseInitConfigModel> callback) {
        repose.initConfig(API_KEY).enqueue(callback);
    }

    public void acceptCall(String token, String channel, Callback<ResponseModel> callback) {
        repose.acceptCall(API_KEY, token, channel).enqueue(callback);
    }

    public void rejectCall(String token, Callback<ResponseModel> callback) {
        repose.rejectCall(API_KEY, token).enqueue(callback);
    }
}
