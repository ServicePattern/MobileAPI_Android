package com.brightpattern.api.chat.events.signalingdata;


public class IceCandidateData extends SignalingData {

    private String sdpMid;

    private int sdpMLineIndex;

    private String candidate;

    public IceCandidateData() {
        super(SignalingDataType.ICE_CANDIDATE);
    }

    public int getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public String getCandidate() {
        return candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public static IceCandidateData create(String sdpMid, int sdpMLineIndex, String candidate) {
        IceCandidateData data = new IceCandidateData();
        data.candidate = candidate;
        data.sdpMid = sdpMid;
        data.sdpMLineIndex = sdpMLineIndex;
        return data;
    }

    @Override
    public String toString() {
        return "IceCandidateData{" +
                "sdpMid='" + sdpMid + '\'' +
                ", sdpMLineIndex=" + sdpMLineIndex +
                ", candidate='" + candidate + '\'' +
                '}';
    }
}
