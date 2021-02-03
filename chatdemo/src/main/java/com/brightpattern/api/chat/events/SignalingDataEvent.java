package com.brightpattern.api.chat.events;


import com.brightpattern.api.chat.events.signalingdata.AnswerCallData;
import com.brightpattern.api.chat.events.signalingdata.CallStateData;
import com.brightpattern.api.chat.events.signalingdata.EndCallData;
import com.brightpattern.api.chat.events.signalingdata.IceCandidateData;
import com.brightpattern.api.chat.events.signalingdata.OfferCallData;
import com.brightpattern.api.chat.events.signalingdata.RequestCallData;
import com.brightpattern.api.chat.events.signalingdata.SignalingData;
import com.brightpattern.api.data.ChatParty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignalingDataEvent extends ChatEvent {

    private static final String TAG = SignalingDataEvent.class.getSimpleName();

    private static class DataDeserializer implements JsonDeserializer<SignalingData> {

        private Map<String, Class> classProvider = new HashMap<String, Class>();

        public DataDeserializer() {
            classProvider.put(SignalingData.SignalingDataType.OFFER_CALL.name(), OfferCallData.class);
            classProvider.put(SignalingData.SignalingDataType.ICE_CANDIDATE.name(), IceCandidateData.class);
            classProvider.put(SignalingData.SignalingDataType.END_CALL.name(), EndCallData.class);
            classProvider.put(SignalingData.SignalingDataType.REQUEST_CALL.name(), RequestCallData.class);
            classProvider.put(SignalingData.SignalingDataType.ANSWER_CALL.name(), AnswerCallData.class);
            classProvider.put(SignalingData.SignalingDataType.CALL_STATE.name(), CallStateData.class);
        }

        @Override
        public SignalingData deserialize(JsonElement src, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String type = ((JsonObject)src).get("type").getAsString();
            Class<? extends SignalingData> blockType = classProvider.get(type);
            SignalingData block = context.deserialize(src, blockType);
            return block;
        }
    }

    private Date date;

    private ChatParty party;

    private SignalingData data;

    protected SignalingDataEvent() {
        super(Type.SIGNALLING_DATA);
    }

    public SignalingData getData() {
        return data;
    }

    public Date getDate() {
        return date;
    }

    public ChatParty getParty() {
        return party;
    }

    public static ChatEvent create(ChatParty chatParty, Date timestamp, JsonObject json) {
        SignalingDataEvent event = new SignalingDataEvent();
        event.date = timestamp;
        event.party = chatParty;

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SignalingData.class, new DataDeserializer())
                .create();
        event.data = gson.fromJson(json, SignalingData.class);
        return event;
    }
}
