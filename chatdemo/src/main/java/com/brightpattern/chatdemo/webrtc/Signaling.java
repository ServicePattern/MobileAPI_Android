package com.brightpattern.chatdemo.webrtc;


public interface Signaling {
    void sendAnswer(String sdp);
    void sendIceCandidate(String id, int index, String candidate);
    void sendCallState(boolean audioEnable, boolean videoEnabled);
    void requestCall(boolean offerVideo);
    void endCall();
    void addListener(SignalingListener listener);
    void removeListener(SignalingListener listener);

    interface SignalingListener {
        void onOffer(String sdp);
        void onIceCandidate(String id, int index, String candidate);
        void onCallState(boolean audioEnabled, boolean videoEnabled);
        void onEndCall();
    }
}

