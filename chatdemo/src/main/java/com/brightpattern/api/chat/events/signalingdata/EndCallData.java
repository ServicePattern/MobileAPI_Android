package com.brightpattern.api.chat.events.signalingdata;


public class EndCallData extends SignalingData {

    public EndCallData() {
        super(SignalingDataType.END_CALL);
    }

    public static EndCallData create() {
        return new EndCallData();
    }


}
