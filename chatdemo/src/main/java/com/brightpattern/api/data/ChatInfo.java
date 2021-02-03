package com.brightpattern.api.data;

public class ChatInfo {
	
	public static enum State {UNDEFINED, CONNECTING, QUEUED, CONNECTED, DISCONNECTED}
	
	private String chat_id;
	private String state;
	private String ewt;
	private Boolean i_new_chat;
	private String phone_number;
	
	public String getChatId() {
		return chat_id;
	}
	
	public State getState() {
		if ("queued".equals(state)) {
			return State.QUEUED;
		} else if ("connected".equals(state)) {
			return State.CONNECTED;
        } else if ("connecting".equals(state)) {
            return State.CONNECTING;
        }
		return State.UNDEFINED;
	}	
	
	public String getEwt() {
		return ewt;
	}
	
	public Boolean isNewChat() {
		return i_new_chat;
	}
	
	public String getPhoneNumber() {
		return phone_number;
	}
}
