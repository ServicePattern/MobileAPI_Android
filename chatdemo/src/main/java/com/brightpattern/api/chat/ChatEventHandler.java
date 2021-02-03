package com.brightpattern.api.chat;


import com.brightpattern.api.chat.events.ChatEvent;

public interface ChatEventHandler {
    void onEvent(ChatEvent event);
}
