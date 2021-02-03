package com.brightpattern.api.chat.events;


import com.brightpattern.api.data.ChatInfo.State;

public class StateChangeEvent extends ChatEvent {

    private State state;

    protected StateChangeEvent() {
        super(Type.STATE_CHANGE);
    }

    public State getState() {
        return state;
    }

    public static StateChangeEvent create(State state) {
        StateChangeEvent event = new StateChangeEvent();
        event.state = state;
        return event;
    }
}
