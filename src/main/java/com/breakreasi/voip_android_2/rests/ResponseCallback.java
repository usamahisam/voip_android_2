package com.breakreasi.voip_android_2.rests;

import androidx.annotation.Nullable;

public interface ResponseCallback<T> {
    void onResponse(@Nullable T response);
}
