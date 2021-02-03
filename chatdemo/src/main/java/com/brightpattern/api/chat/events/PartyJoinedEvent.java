package com.brightpattern.api.chat.events;

import com.brightpattern.api.data.ChatParty;

import java.util.Date;

public class PartyJoinedEvent extends ChatEvent implements IsVisualEvent {

    private Date date;

    private ChatParty party;

    protected PartyJoinedEvent() {
        super(Type.PARTY_JOINED);
    }

    public Date getDate() {
        return date;
    }

    public ChatParty getParty() {
        return party;
    }

    public static PartyJoinedEvent create(Date date, ChatParty party) {
        PartyJoinedEvent event = new PartyJoinedEvent();
        event.party = party;
        event.date = date;
        return event;
    }

    @Override
    public ChatVisualEvent toVisualEvent() {
        return ChatVisualEvent.createPartyJoined(date, party);
    }
}
