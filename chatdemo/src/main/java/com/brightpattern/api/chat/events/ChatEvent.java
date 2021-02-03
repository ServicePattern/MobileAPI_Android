package com.brightpattern.api.chat.events;


public class ChatEvent {

    public static enum Type {STATE_CHANGE, ERROR, SESSION_ENDED, PARTY_JOINED, PARTY_LEFT, MESSAGE, FILE,
                             STOP_TYPING, START_TYPING, SHOW_FORM, TIMEOUT_WARNING, INACTIVITY_TIMEOUT, SIGNALLING_DATA
    }

    private Type type;

    protected ChatEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public  <T extends ChatEvent> T cast() {
        return (T)this;
    }
}
