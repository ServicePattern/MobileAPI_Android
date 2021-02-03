package com.brightpattern.api.data;


public class Availability {
	
	private String chat;
	
	public boolean isChatAvailable() {
		return "available".equals(chat);
	}	
}
