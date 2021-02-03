package com.brightpattern.api.chat.events;


import com.brightpattern.api.data.ChatInfo;

public class SessionEndedEvent extends ChatEvent {

    private ChatInfo.State state;

    protected SessionEndedEvent() {
        super(Type.SESSION_ENDED);
    }

    public ChatInfo.State getState() {
        return state;
    }

    public static SessionEndedEvent create() {
        return new SessionEndedEvent();
    }
}