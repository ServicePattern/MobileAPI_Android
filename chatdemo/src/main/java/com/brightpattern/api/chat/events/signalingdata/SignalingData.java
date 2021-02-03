package com.brightpattern.api.chat.events.signalingdata;


public class SignalingData {

    public static enum SignalingDataType {REQUEST_CALL, OFFER_CALL, ANSWER_CALL, ICE_CANDIDATE, END_CALL, CALL_STATE}

    private final SignalingDataType type;

    public SignalingData(SignalingDataType type) {
        this.type = type;
    }

    public SignalingDataType getType() {
        return type;
    }

    public  <T extends SignalingData> T cast() {
        return (T)this;
    }

    @Override
    public String toString() {
        return "SessionData{" +
                "type=" + type +
                '}';
    }
}
