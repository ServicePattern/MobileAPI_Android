package com.brightpattern.chatdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


import com.brightpattern.api.Chat;
import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.events.ChatEvent;
import com.brightpattern.api.chat.events.StateChangeEvent;
import com.brightpattern.api.data.ChatInfo;
import com.brightpattern.chatdemo.webrtc.WebRtcConnection;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class VideoCallActivity extends Activity {

    private static final String TAG = VideoCallActivity.class.getSimpleName();

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    public static final String AUDIO_CALL = TAG + "audioCall";
    public static final String VIDEO_CALL = TAG + "videoCall";

    private boolean controlsVisible = false;

    private WebRtcConnection webRtcConnection;
    
    private View controlsView;

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            changeControlsVisibility(false);
        }
    };
    private Button endCallButton;
    private Chat chatWrapper;
    private ChatEventHandler chatHandler;
    private WebRtcConnection.StateListener webRtcConnectionStateListener;
    private SurfaceViewRenderer remoteRenderer;
    private SurfaceViewRenderer localRenderer;
    private EglBase rootEglBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webRtcConnection = ((DemoApplication)getApplication()).getWebRtcConnection();

        chatWrapper = ((DemoApplication)getApplication()).getChatWrapper();

        setContentView(R.layout.activity_call);
        setupActionBar();

        controlsView = findViewById(R.id.content_controls);
        remoteRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);
        localRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
        endCallButton = (Button) findViewById(R.id.end_call_button);

        rootEglBase = EglBase.create();
        localRenderer.init(rootEglBase.getEglBaseContext(), null);
        localRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                
        remoteRenderer.init(rootEglBase.getEglBaseContext(), null);
        remoteRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        localRenderer.setZOrderMediaOverlay(true);
        localRenderer.setEnableHardwareScaler(true /* enabled */);
        remoteRenderer.setEnableHardwareScaler(true /* enabled */);

        // Set up the user interaction to manually show or hide the system UI.
        remoteRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeControlsVisibility(!controlsVisible);
            }
        });

        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webRtcConnection.endCall();
                NavUtils.navigateUpFromSameTask(VideoCallActivity.this);
            }
        });

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e(TAG, "Uncaught exception", ex);
            }
        });

        chatHandler = new ChatEventHandler() {
            @Override
            public void onEvent(ChatEvent event) {
                if (event.getType() == ChatEvent.Type.STATE_CHANGE) {
                    StateChangeEvent e = event.cast();
                    if (e.getState() == ChatInfo.State.DISCONNECTED) {
                        goUpAndFinish();
                    }
                }
            }
        };

        webRtcConnectionStateListener = new WebRtcConnection.StateListener() {
            @Override
            public void onLocalStreamStateChange(boolean audioEnabled, boolean videoEnabled) {
                //TODO
            }

            @Override
            public void onRemoteStreamStateChange(boolean audioEnabled, boolean videoEnabled) {
                //TODO
            }

            @Override
            public void onConnectionStateChange(WebRtcConnection.State state) {
                if (state == WebRtcConnection.State.NOT_ACTIVE) {
                    goUpAndFinish();
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.videocall_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                // TODO: If Settings has multiple levels, Up should navigate up
                // that hierarchy.
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.action_change_camera:
                webRtcConnection.changeCamera();
                break;
            case R.id.action_mute:
                webRtcConnection.mute();
                break;
            case R.id.action_unmute:
                webRtcConnection.unmute();
                break;
            case R.id.action_disable_video:
                webRtcConnection.disableVideo();
                break;
            case R.id.action_enable_video:
                webRtcConnection.enableVideo();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem disableVideoItem = menu.findItem(R.id.action_disable_video);
        MenuItem enableVideoItem = menu.findItem(R.id.action_enable_video);
        MenuItem muteItem = menu.findItem(R.id.action_mute);
        MenuItem unmuteItem = menu.findItem(R.id.action_unmute);

        boolean muted = webRtcConnection.isMuted();
        muteItem.setVisible(!muted);
        unmuteItem.setVisible(muted);

        boolean localVideoEnabled = webRtcConnection.isLocalVideoEnabled();
        disableVideoItem.setVisible(localVideoEnabled);
        enableVideoItem.setVisible(!localVideoEnabled);

        return true;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void changeControlsVisibility(boolean visible) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int controlsHeight = controlsView.getHeight();
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            controlsView.animate()
                    .translationY(visible ? 0 : controlsHeight)
                    .setDuration(shortAnimTime);
        } else {
            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        if (visible) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
        controlsVisible = visible;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        controlsView.setVisibility(View.VISIBLE);
        controlsView.setTranslationY(0);
        controlsVisible = true;
        delayedHide(100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatWrapper.addChatEventHandler(chatHandler);
        webRtcConnection.addStateListener(webRtcConnectionStateListener);

        Intent intent = getIntent();
        boolean audioCall = intent.getExtras().containsKey(AUDIO_CALL);
        boolean videoCall = intent.getExtras().containsKey(VIDEO_CALL);
        boolean callRequested = audioCall || videoCall;

        if (!webRtcConnection.isActive() && !callRequested) {
            goUpAndFinish();
        } else {
            webRtcConnection.init(this, videoCall);
            webRtcConnection.registerSignallingListener();
            webRtcConnection.setVideoRenderCallbacks(localRenderer, remoteRenderer);
            if (!webRtcConnection.isActive()) {
                webRtcConnection.sendCallRequest(videoCall);
            }
        }
    }

    private void goUpAndFinish() {
        NavUtils.navigateUpFromSameTask(VideoCallActivity.this);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatWrapper.removeChatEventHandler(chatHandler);
        webRtcConnection.removeStateListener(webRtcConnectionStateListener);
        webRtcConnection.unregisterSignallingListener();
        webRtcConnection.removeAllVideoRenderCallbacks();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
