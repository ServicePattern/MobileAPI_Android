package com.brightpattern.api.chat.events;


import com.brightpattern.api.chat.EventCreator.FileType;
import com.brightpattern.api.data.ChatParty;

import java.util.Date;

public class FileEvent extends ChatEvent implements IsVisualEvent {

    private ChatParty party;

    private Date date;

    private FileType fileType;

    private String fileId;

    protected FileEvent() {
        super(Type.FILE);
    }

    public static FileEvent create(ChatParty party, Date date, FileType fileType, String fileId) {
        FileEvent event = new FileEvent();
        event.party = party;
        event.date = date;
        event.fileType = fileType;
        event.fileId = fileId;
        return event;
    }

    public static FileEvent create(ChatParty party, Date date, String fileType, String fileId) {
        FileType type = "image".equals(fileType)?FileType.IMAGE:FileType.ATTACHMENT;
        return create(party, date, type, fileId);
    }

    public Date getDate() {
        return date;
    }

    public FileType getFileType() {
        return fileType;
    }

    public ChatParty getParty() {
        return party;
    }

    public String getFileId() {
        return fileId;
    }

    @Override
    public ChatVisualEvent toVisualEvent() {
        return ChatVisualEvent.createFile(date, party, fileId);
    }
}
