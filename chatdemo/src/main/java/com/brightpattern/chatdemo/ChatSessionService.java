package com.brightpattern.chatdemo;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.brightpattern.api.Chat;
import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.events.ChatEvent;
import com.brightpattern.api.chat.events.MessageEvent;
import com.brightpattern.api.chat.events.ShowFormEvent;
import com.brightpattern.api.chat.events.StateChangeEvent;
import com.brightpattern.api.data.ChatInfo;
import com.brightpattern.chatdemo.webrtc.WebRtcConnection;

import java.util.List;

public class ChatSessionService extends Service {

    private static final String TAG = ChatSessionService.class.getSimpleName();

    public static interface ChatSessionServiceInterface {
        void setChatUIVisibility(boolean visibility);
    }

    public class ChatSessionBinder extends Binder {

        private ChatSessionServiceInterface service;

        public ChatSessionBinder(ChatSessionServiceInterface service) {
            this.service = service;
        }

        public ChatSessionServiceInterface getService() {
            return  service;
        }
    }

    private Chat chatWrapper;

    private ChatSessionBinder chatSessionBinder;

    private ChatEventHandler chatHandler;

    private int NOTIFICATION_ID = 1;

    private WebRtcConnection webRtcConnection;

    private int newMessages = 0;

    private volatile boolean chatUIVisible = false;

    public ChatSessionService() {
        this.chatSessionBinder = new ChatSessionBinder(new ChatSessionServiceInterface() {
            @Override
            public void setChatUIVisibility(boolean visibility) {
                chatUIVisible = visibility;
                if (chatUIVisible) {
                    newMessages = 0;
                    clearNotification();
                }
            }
        });
    }

    private boolean isApplicationActive() {
        ActivityManager am = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity.getPackageName().equals(getApplication().getPackageName())) {
                return true;
            }
        }
        return false;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        newMessages = 0;

        webRtcConnection = ((DemoApplication)getApplication()).getWebRtcConnection();

        chatWrapper =  ((DemoApplication)getApplication()).getChatWrapper();
        chatHandler = new ChatEventHandler() {
            @Override
            public void onEvent(ChatEvent event) {
                if (event.getType() == ChatEvent.Type.STATE_CHANGE) {
                    StateChangeEvent e = event.cast();
                    if (e.getState() == ChatInfo.State.DISCONNECTED) {
                        newMessages = 0;
                        ChatSessionService.this.stopSelf();
                        if (webRtcConnection.isActive()) {
                            webRtcConnection.endCall();
                        }
                    }
                } else if (event.getType() == ChatEvent.Type.SHOW_FORM && isApplicationActive()) {
                    Intent intent = new Intent(getApplication(), ChatSurveyActivity.class);
                    intent.putExtra(ChatSurveyActivity.SHOW_FORM, ((ShowFormEvent)event).getFormDate());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (event.getType() == ChatEvent.Type.MESSAGE && !chatUIVisible) {
                    newMessages++;
                    MessageEvent messageEvent = event.cast();
                    showNewMessageNotification(messageEvent.getMessage());
                }
            }
        };
        chatWrapper.addChatEventHandler(chatHandler);

        if (!chatWrapper.isActiveState()) {
            stopSelf();
        }

        NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Active chat");

        mBuilder.setContentIntent(getOpenChatUIIntent());

        startForeground(NOTIFICATION_ID, mBuilder.build());
    }

    private PendingIntent getOpenChatUIIntent() {
        Intent resultIntent = new Intent(this, ChatActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void showNewMessageNotification(String message) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(newMessages + " new messages")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(getOpenChatUIIntent());
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }

    private void clearNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Active chat")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(getOpenChatUIIntent());
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return chatSessionBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatWrapper.removeChatEventHandler(chatHandler);
        Log.d(TAG, "onDestroy");
    }
}
