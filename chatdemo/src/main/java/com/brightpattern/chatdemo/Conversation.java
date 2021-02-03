package com.brightpattern.chatdemo;

import com.brightpattern.api.chat.events.ChatVisualEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Conversation implements Serializable {

    private static final long serialVersionUID = 0L;

    private List<ChatVisualEvent> events;

    private Date date;

    public Conversation() {
        events = new ArrayList<ChatVisualEvent>();
    }

    public Conversation(List<ChatVisualEvent> events, Date date) {
        this.events = events;
        this.date = date;
    }

    public List<ChatVisualEvent> getMessages() {
        return events;
    }

    public void setMessages(List<ChatVisualEvent> events) {
        this.events = events;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
