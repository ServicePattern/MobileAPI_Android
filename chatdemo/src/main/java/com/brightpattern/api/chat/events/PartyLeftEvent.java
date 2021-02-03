package com.brightpattern.api.chat.events;


import com.brightpattern.api.data.ChatParty;

import java.util.Date;

public class PartyLeftEvent extends ChatEvent implements IsVisualEvent {

    private Date date;

    private ChatParty party;

    protected PartyLeftEvent() {
        super(Type.PARTY_LEFT);
    }

    public Date getDate() {
        return date;
    }

    public ChatParty getParty() {
        return party;
    }

    public static PartyLeftEvent create(Date date, ChatParty party) {
        PartyLeftEvent event = new PartyLeftEvent();
        event.party = party;
        event.date = date;
        return event;
    }

    @Override
    public ChatVisualEvent toVisualEvent() {
        return ChatVisualEvent.createPartyLeft(date, party);
    }
}
