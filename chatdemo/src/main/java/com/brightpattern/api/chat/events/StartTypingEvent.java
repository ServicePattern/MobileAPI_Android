package com.brightpattern.api.chat.events;

import com.brightpattern.api.data.ChatParty;

public class StartTypingEvent extends ChatEvent {

    private ChatParty party;

    protected StartTypingEvent() {
        super(Type.START_TYPING);
    }

    public ChatParty getParty() {
        return party;
    }

    public static StartTypingEvent create(ChatParty party) {
        StartTypingEvent event = new StartTypingEvent();
        event.party = party;
        return event;
    }
}

