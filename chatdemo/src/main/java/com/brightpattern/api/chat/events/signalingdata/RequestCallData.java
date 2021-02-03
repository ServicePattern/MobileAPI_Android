package com.brightpattern.api.chat.events.signalingdata;

public class RequestCallData extends SignalingData {

    private boolean offerVideo;

    public RequestCallData() {
        super(SignalingDataType.REQUEST_CALL);
    }

    public boolean getOfferVideo() {
        return offerVideo;
    }

    public static RequestCallData create(boolean offerVideo) {
        RequestCallData data = new RequestCallData();
        data.offerVideo = offerVideo;
        return data;
    }

    @Override
    public String toString() {
        return "RequestCallData{" +
                "offerVideo=" + offerVideo +
                '}';
    }
}
