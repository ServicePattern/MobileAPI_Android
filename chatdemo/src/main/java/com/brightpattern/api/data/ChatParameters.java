package com.brightpattern.api.data;

import java.util.HashMap;
import java.util.Map;

public class ChatParameters {
	
	private String phone_number;
	private Map<String, Object> parameters = new HashMap<String, Object>();

    public void setLocation(double latitude, double longitude) {
        Map<String, String> location = new HashMap<String, String>();
        location.put("latitude", Double.toString(latitude));
        location.put("longitude", Double.toString(longitude));
        parameters.put("location", location);
    }

    public void setFirstName(String firstName) {
        parameters.put("first_name", firstName);
    }

    public void setlastName(String lastName) {
        parameters.put("last_name", lastName);
    }

	public String getPhoneNumber() {
		return phone_number;
	}

	public void setPhoneNumber(String phone_number) {
		this.phone_number = phone_number;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
