package com.brightpattern.api.chat.events;


import com.brightpattern.api.data.ChatParty;

public class StopTypingEvent extends ChatEvent {

    private ChatParty party;

    protected StopTypingEvent() {
        super(Type.STOP_TYPING);
    }

    public ChatParty getParty() {
        return party;
    }

    public static StopTypingEvent create(ChatParty party) {
        StopTypingEvent event = new StopTypingEvent();
        event.party = party;
        return event;
    }
}
