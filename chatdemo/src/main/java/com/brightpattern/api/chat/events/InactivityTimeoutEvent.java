package com.brightpattern.api.chat.events;


import java.util.Date;

public class InactivityTimeoutEvent extends ChatEvent implements IsVisualEvent {

    private String message;

    private Date date;

    protected InactivityTimeoutEvent() {
        super(Type.INACTIVITY_TIMEOUT);
    }

    public static InactivityTimeoutEvent create(Date date, String message) {
        InactivityTimeoutEvent event = new InactivityTimeoutEvent();
        event.date = date;
        event.message = message;
        return event;
    }

    @Override
    public ChatVisualEvent toVisualEvent() {
        return ChatVisualEvent.createInboundMessage(date, message);
    }
}
