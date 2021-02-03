package com.brightpattern.api.chat.events.signalingdata;


public class CallStateData extends SignalingData {

    private boolean audioEnabled = true;

    private boolean videoEnabled = true;

    public CallStateData() {
        super(SignalingDataType.CALL_STATE);
    }

    public boolean getAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public boolean getVideoEnabled() {
        return videoEnabled;
    }

    public void setVideoEnabled(boolean audioEnabled) {
        this.videoEnabled = audioEnabled;
    }

    public static CallStateData create(boolean audioEnabled, boolean videoEnabled) {
        CallStateData data = new CallStateData();
        data.setAudioEnabled(audioEnabled);
        data.setVideoEnabled(videoEnabled);
        return data;
    }
}
