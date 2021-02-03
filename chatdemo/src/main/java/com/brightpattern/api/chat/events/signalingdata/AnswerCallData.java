package com.brightpattern.api.chat.events.signalingdata;


public class AnswerCallData extends SignalingData {

    private String sdp;

    public AnswerCallData() {
        super(SignalingDataType.ANSWER_CALL);
    }

    public String getSdp() {
        return sdp;
    }

    public static AnswerCallData create(String sdp) {
        AnswerCallData data = new AnswerCallData();
        data.sdp = sdp;
        return data;
    }

    @Override
    public String toString() {
        return "AnswerCallData{" +
                "sdp='" + sdp + '\'' +
                '}';
    }
}
