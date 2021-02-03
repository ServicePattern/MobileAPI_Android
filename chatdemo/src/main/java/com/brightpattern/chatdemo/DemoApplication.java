package com.brightpattern.chatdemo;


import android.app.AlertDialog;
import android.app.Application;
import android.os.Handler;

import com.brightpattern.api.Chat;
import com.brightpattern.api.ChatImpl;
import com.brightpattern.api.data.ConnectionConfig;
import com.brightpattern.chatdemo.webrtc.SignalingChatAdapter;
import com.brightpattern.chatdemo.webrtc.WebRtcConnection;

import android.provider.Settings.Secure;
import android.util.Log;

public class DemoApplication extends Application {

    private static final String TAG = DemoApplication.class.getSimpleName();

    private Chat chatWrapper;
    private WebRtcConnection webRtcConnection;

    @Override
    public void onCreate() {
        super.onCreate();

        Handler handler = new Handler();

        chatWrapper = new ChatImpl(handler);

        initChatWrapper();

        webRtcConnection = new WebRtcConnection(new SignalingChatAdapter(chatWrapper), handler);
    }

    public void initChatWrapper() {
        Settings settings = Settings.load(this);

        initClientId(settings);

        if (settings.isValidConnectionSettings()) {
            ConnectionConfig cfg = new ConnectionConfig(settings.getServerAddress(), settings.getTenant(), settings.getAppID(), settings.getClientId());
            try {
                chatWrapper.init(cfg);
            } catch (Exception e) {
                Log.e(TAG, "Init chat", e);
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Init connection error!");
                if (e.getMessage() != null) {
                    builder.setMessage(e.getMessage());
                }
                builder.setNegativeButton("Close", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        }
    }

    //initialize clientId one time for application on current device
    private void initClientId(Settings settings) {
        if (settings.getClientId().isEmpty()) {
            String androidId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
            settings.setClientId(androidId);
            settings.save(this);
        }
    }

    public Chat getChatWrapper() {
        return chatWrapper;
    }

    public WebRtcConnection getWebRtcConnection() {
        return webRtcConnection;
    }
}
