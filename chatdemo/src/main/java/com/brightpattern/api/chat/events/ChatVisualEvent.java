package com.brightpattern.api.chat.events;


import com.brightpattern.api.data.ChatParty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatVisualEvent implements Serializable {

    public static enum Type {TEXT_MESSAGE, FILE_MESSAGE, PARTY_EVENT, TYPING_EVENT}

    public static enum PartyEvent {JOINED, LEFT}

    public static enum Direction {INBOUND, OUTBOUND}

    private Type type = Type.TEXT_MESSAGE;

    private Date date;

    private String text;

    private Direction direction;

    private String fileId;

    private String partyName;

    private PartyEvent partyEventType;

    public  ChatVisualEvent(){
    }

    public ChatVisualEvent(Direction direction, Date date, String text) {
        this.date = date;
        this.text = text;
        this.direction = direction;
    }

    public Type getType() {
        return type;
    }

    public PartyEvent getPartyEventType() {
        return partyEventType;
    }

    public String getPartyName() {
        return partyName;
    }

    public Date getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getFileId() {
        return fileId;
    }

    public static ChatVisualEvent createInboundMessage(Date date, String text) {
        return new ChatVisualEvent(Direction.INBOUND, date, text);
    }

    public static ChatVisualEvent createOutboundMessage(Date date, String text) {
        return new ChatVisualEvent(Direction.OUTBOUND, date, text);
    }

    public static ChatVisualEvent createMessage(Date date, ChatParty party, String text) {
        ChatVisualEvent event;
        if (party.getType() == ChatParty.PartyType.ME) {
            event = createOutboundMessage(date, text);
        } else {
            event = createInboundMessage(date, text);
        }
        event.partyName = party.getDisplayName();
        return event;
    }

    public static ChatVisualEvent createFile(Date date, ChatParty party, String imageFileName) {
        ChatVisualEvent event;
        if (party.getType() == ChatParty.PartyType.ME) {
            event = createOutboundFile(date, imageFileName);
        } else {
            event = createInboundFile(date, imageFileName);
        }
        event.partyName = party.getDisplayName();
        return event;
    }

    public static ChatVisualEvent createInboundFile(Date date, String imageFileName) {
        ChatVisualEvent chatVisualEvent = new ChatVisualEvent(Direction.INBOUND, date, null);
        chatVisualEvent.fileId = imageFileName;
        chatVisualEvent.type = Type.FILE_MESSAGE;
        return chatVisualEvent;
    }

    public static ChatVisualEvent createOutboundFile(Date date, String imageFileName) {
        ChatVisualEvent chatVisualEvent = new ChatVisualEvent(Direction.OUTBOUND, date, null);
        chatVisualEvent.fileId = imageFileName;
        chatVisualEvent.type = Type.FILE_MESSAGE;
        return chatVisualEvent;
    }

    public static ChatVisualEvent createPartyJoined(Date time, ChatParty party) {
        ChatVisualEvent event = new ChatVisualEvent();
        event.type = Type.PARTY_EVENT;
        event.partyEventType = PartyEvent.JOINED;
        event.date = time;
        event.partyName = party.getDisplayName();
        return event;
    }

    public static ChatVisualEvent createPartyLeft(Date time, ChatParty party) {
        ChatVisualEvent event = new ChatVisualEvent();
        event.type = Type.PARTY_EVENT;
        event.partyEventType = PartyEvent.LEFT;
        event.date = time;
        event.partyName = party.getDisplayName();
        return event;
    }

    public static ChatVisualEvent createTypingEvent(ChatParty party) {
        ChatVisualEvent event = new ChatVisualEvent();
        event.type = Type.TYPING_EVENT;
        event.partyEventType = PartyEvent.LEFT;
        event.partyName = party.getDisplayName();
        return event;
    }

    public static List<ChatVisualEvent> adapt(List<ChatEvent> events) {
        List<ChatVisualEvent> result = new ArrayList<ChatVisualEvent>();

        for (ChatEvent e : events) {
            if (e instanceof IsVisualEvent) {
                result.add(((IsVisualEvent) e).toVisualEvent());
            }
        }
        return result;
    }
}
