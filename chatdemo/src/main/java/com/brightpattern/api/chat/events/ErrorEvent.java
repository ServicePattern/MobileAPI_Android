package com.brightpattern.api.chat.events;

public class ErrorEvent extends ChatEvent {

    private Throwable exception;

    protected ErrorEvent() {
        super(Type.ERROR);
    }

    public Throwable getException() {
        return exception;
    }

    public static ErrorEvent create(Throwable t) {
        ErrorEvent event = new ErrorEvent();
        event.exception = t;
        return event;
    }
}
