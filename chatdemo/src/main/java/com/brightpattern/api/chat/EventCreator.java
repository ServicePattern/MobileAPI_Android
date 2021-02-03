package com.brightpattern.api.chat;

import java.util.UUID;

import com.brightpattern.api.chat.events.signalingdata.SignalingData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EventCreator {

    public static enum FileType {IMAGE, ATTACHMENT}

    public static JsonObject createSignallingData(SignalingData sessionData, String destPartyId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("event", "chat_session_signaling");
        obj.addProperty("msg_id", UUID.randomUUID().toString());
        obj.addProperty("destination_party_id", destPartyId);
        JsonElement data = new Gson().toJsonTree(sessionData);
        obj.add("data", data);
        return obj;
    }

	public static JsonObject createMessage(String message) {
		JsonObject obj = new JsonObject();		
		obj.addProperty("event", "chat_session_message");
		obj.addProperty("msg_id", UUID.randomUUID().toString());
		obj.addProperty("msg", message);
		return obj;		
	}

    public static JsonObject createFile(FileType fileType, String fileId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("event", "chat_session_file");
        obj.addProperty("msg_id", UUID.randomUUID().toString());
        obj.addProperty("file_type", fileType == FileType.IMAGE?"image":"attachment");
        obj.addProperty("file_id", fileId);
        return obj;
    }

	public static JsonObject startTyping() {
		JsonObject obj = new JsonObject();		
		obj.addProperty("event", "chat_session_typing");		
		return obj;
	}

	public static JsonObject stopTyping() {
		JsonObject obj = new JsonObject();		
		obj.addProperty("event", "chat_session_not_typing");		
		return obj;
	}

	public static JsonObject endSession() {
		JsonObject obj = new JsonObject();		
		obj.addProperty("event", "chat_session_end");		
		return obj;
	}

    public static JsonObject disconnectSession() {
        JsonObject obj = new JsonObject();
        obj.addProperty("event", "chat_session_disconnect");
        return obj;
    }
}
