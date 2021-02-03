package com.brightpattern.chatdemo.webrtc;


import com.brightpattern.api.Chat;
import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.events.ChatEvent;
import com.brightpattern.api.chat.events.SignalingDataEvent;
import com.brightpattern.api.chat.events.signalingdata.AnswerCallData;
import com.brightpattern.api.chat.events.signalingdata.CallStateData;
import com.brightpattern.api.chat.events.signalingdata.IceCandidateData;
import com.brightpattern.api.chat.events.signalingdata.OfferCallData;
import com.brightpattern.api.chat.events.signalingdata.SignalingData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SignalingChatAdapter implements Signaling {

    private final Chat chat;

    private List<SignalingListener> listeners = new CopyOnWriteArrayList<SignalingListener>();

    public SignalingChatAdapter(Chat chat) {
        this.chat = chat;
        chat.addChatEventHandler(new ChatEventHandler() {
            @Override
            public void onEvent(ChatEvent event) {
                if (event instanceof SignalingDataEvent) {
                    SignalingDataEvent sdEvent = event.cast();
                    SignalingData sd = sdEvent.getData();
                    switch (sd.getType()) {
                        case OFFER_CALL:
                            OfferCallData ocData = sd.cast();
                            callListenersOnOffer(ocData);
                            break;
                        case ICE_CANDIDATE:
                            IceCandidateData iceData = sd.cast();
                            callListenersIceCandidate(iceData);
                            break;
                        case END_CALL:
                            callListenersOnEndCall();
                            break;
                        case CALL_STATE:
                            CallStateData csData = sd.cast();
                            callListenersCallState(csData);
                            break;
                    }
                }
            }
        });
    }

    private void callListenersCallState(CallStateData csData) {
        for (SignalingListener listener : listeners) {
            listener.onCallState(csData.getAudioEnabled(), csData.getVideoEnabled());
        }
    }

    private void callListenersIceCandidate(IceCandidateData data) {
        for (SignalingListener listener : listeners) {
            listener.onIceCandidate(data.getSdpMid(), data.getSdpMLineIndex(), data.getCandidate());
        }
    }

    private void callListenersOnOffer(OfferCallData data) {
        for (SignalingListener listener : listeners) {
            listener.onOffer(data.getSdp());
        }
    }

    private void callListenersOnEndCall() {
        for (SignalingListener listener : listeners) {
            listener.onEndCall();
        }
    }

    @Override
    public void sendAnswer(String sdp) {
        chat.sendCallAnswer(AnswerCallData.create(sdp));
    }

    @Override
    public void sendIceCandidate(String id, int index, String candidate) {
        chat.sendIceCandidate(IceCandidateData.create(id, index, candidate));
    }

    @Override
    public void sendCallState(boolean audioEnable, boolean videoEnabled) {
        chat.sendCallState(CallStateData.create(audioEnable, videoEnabled));
    }

    @Override
    public void requestCall(boolean offerVideo) {
        chat.sendCallRequest(offerVideo);
    }

    @Override
    public void endCall() {
        chat.sendEndCall();
    }

    @Override
    public void addListener(SignalingListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(SignalingListener listener) {
        listeners.remove(listener);
    }
}
