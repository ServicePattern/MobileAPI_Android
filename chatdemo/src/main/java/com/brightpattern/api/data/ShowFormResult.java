package com.brightpattern.api.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

public class ShowFormResult implements Serializable {
	
	private String id;
	
	private String name;
	
	private Map<String, String> results = new HashMap<String, String>();
	
	public ShowFormResult() {	
	}

	public ShowFormResult(String id, String name) {	
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setResultValue(String name, String value) {
		results.put(name, value);
	}
	
	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		
		obj.addProperty("event", "chat_session_form_data");
		obj.addProperty("form_request_id", id);
		obj.addProperty("form_name", name);
		
		JsonObject map = new JsonObject();
		for (String name : results.keySet()) {
			map.addProperty(name, results.get(name));
		}		
		obj.add("data", map);
		
		return obj;
	}
}
