package com.breakreasi.voip_android_2.rests;

public class ResponseInitConfigModel {
    final String agora_app_id;

    public ResponseInitConfigModel(String agora_app_id) {
        this.agora_app_id = agora_app_id;
    }

    public String getAgora_app_id() {
        return agora_app_id;
    }
}
