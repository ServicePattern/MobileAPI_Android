package com.brightpattern.api.data;

import com.google.gson.JsonObject;

import java.io.Serializable;

public class ShowFormData implements Serializable {
	
	private String requestId;
	
	private String name;
	
	private int timeout = 0;
	
	private ShowFormData() {	
	}

	public ShowFormData(String requestId, String name, int timeout) {
		this.requestId = requestId;
		this.name = name;
		this.timeout = timeout;
	}

	public String getRequestId() {
		return requestId;
	}

	public String getName() {
		return name;
	}

	public int getTimeout() {
		return timeout;
	}
	
	public static final ShowFormData create(JsonObject obj) {
		ShowFormData data = new ShowFormData();
		
		data.requestId    = obj.get("form_request_id").getAsString();
		data.name         = obj.get("form_name").getAsString();
		
		if (obj.has("form_timeout")) {
			data.timeout = obj.get("form_timeout").getAsInt();
		}		 	 		
		return data;
	}
}
