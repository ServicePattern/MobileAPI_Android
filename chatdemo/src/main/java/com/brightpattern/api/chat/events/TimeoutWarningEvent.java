package com.brightpattern.api.chat.events;

import java.util.Date;

public class TimeoutWarningEvent extends ChatEvent implements IsVisualEvent {

    private Date date;

    private String message;

    protected TimeoutWarningEvent() {
        super(Type.TIMEOUT_WARNING);
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public static TimeoutWarningEvent create(Date date, String message) {
        TimeoutWarningEvent event = new TimeoutWarningEvent();
        event.date = date;
        event.message = message;                
        return event;
    }

    @Override
    public ChatVisualEvent toVisualEvent() {
        return ChatVisualEvent.createInboundMessage(date, message);
    }
}
