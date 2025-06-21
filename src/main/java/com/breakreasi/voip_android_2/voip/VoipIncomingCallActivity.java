package com.breakreasi.voip_android_2.voip;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.breakreasi.voip_android_2.R;

public class VoipIncomingCallActivity extends AppCompatActivity {

    TextView tv_display_name, tv_call_status;
    AppCompatImageView btn_startcall, btn_endcall;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voip_incoming_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_display_name = findViewById(R.id.tv_display_name);
        tv_call_status = findViewById(R.id.tv_call_status);
        btn_startcall = findViewById(R.id.btn_startcall);
        btn_endcall = findViewById(R.id.btn_endcall);

        tv_display_name.setText(getIntent().getStringExtra("displayName"));
        tv_call_status.setText("Panggilan masuk...");

        btn_startcall.setOnClickListener(v -> {
            if (VoipManager.INSTANCE.getVoip() != null) {
                VoipManager.INSTANCE.getVoip().handlerNotificationAccept();
            }
            finish();
        });

        btn_endcall.setOnClickListener(v -> {
            if (VoipManager.INSTANCE.getVoip() != null) {
                VoipManager.INSTANCE.getVoip().handlerNotificationDecline(false);
                VoipManager.INSTANCE.getVoip().decline();
            }
            finish();
        });

    }
}