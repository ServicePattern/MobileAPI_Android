package com.brightpattern.api.chat.events.signalingdata;


public class OfferCallData extends SignalingData {

    private boolean offerVideo;

    private String sdp;

    public OfferCallData() {
        super(SignalingDataType.OFFER_CALL);
    }

    public static OfferCallData create(String sdp, boolean offerVideo) {
        OfferCallData data = new OfferCallData();
        data.sdp = sdp;
        data.offerVideo = offerVideo;
        return data;
    }

    public String getSdp() {
        return sdp;
    }

    public boolean getOfferVideo() {
        return offerVideo;
    }


    @Override
    public String toString() {
        return "OfferCallData{" +
                "offerVideo=" + offerVideo +
                ", sdp='" + sdp + '\'' +
                '}';
    }
}
