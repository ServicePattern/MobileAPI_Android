package com.brightpattern.api.chat.events;


import com.brightpattern.api.data.ChatParty;

import java.util.Date;

public class MessageEvent extends ChatEvent implements IsVisualEvent {

    private Date date;

    private String message;

    private ChatParty party;

    protected MessageEvent() {
        super(Type.MESSAGE);
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public static MessageEvent create(ChatParty party, Date date, String message) {
        MessageEvent event = new MessageEvent();
        event.date = date;
        event.message = message;
        event.party = party;
        return event;
    }

    @Override
    public ChatVisualEvent toVisualEvent() {
        return ChatVisualEvent.createMessage(date, party, message);
    }
}
