package com.brightpattern.api.data;

import com.brightpattern.api.utils.JSONUtils;
import com.google.gson.JsonObject;

public class ChatParty {

    public static final ChatParty ME = new ChatParty("", "", "", "", PartyType.ME);
	
	public static enum PartyType {SCENARIO, EXTERNAL, INTERNAL, ME, UNDEFINED}
	
	private String id;
	
	private String firstName;
	
	private String lastName;
	
	private String displayName;
	
	private PartyType type = PartyType.EXTERNAL;

	private ChatParty(){		
	}
	
	public ChatParty(String id, String firstName, String lastName, String displayName, PartyType type) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.displayName = displayName;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public PartyType getType() {
		return type;
	}
	
	public static final ChatParty create(JsonObject obj) {
		ChatParty party = new ChatParty();
		
		party.id           = JSONUtils.getString(obj,"party_id");
		party.firstName    = JSONUtils.getString(obj,"first_name");
		party.lastName     = JSONUtils.getString(obj,"last_name");
		party.displayName  = JSONUtils.getString(obj,"display_name");
		
		String partyType = JSONUtils.getString(obj,"type");
		if ("scenario".equals(partyType)) {
			party.type = PartyType.SCENARIO;
		} else if ("external".equals(partyType)) {
			party.type = PartyType.EXTERNAL;
		} else if ("internal".equals(partyType)) {
			party.type = PartyType.INTERNAL;
		} 	 		
		return party;
	}
}
