package com.brightpattern.chatdemo;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationsAdapter {

    private static final String TAG = ConversationsAdapter.class.getSimpleName();

    private static String FILE_RECORDS = "pastBase";

    private List<Conversation> conversations;

    private Context сontext;

    public ConversationsAdapter(Context context) {
        conversations = new ArrayList<Conversation>();
        сontext = context;

        try {
            FileInputStream fis = сontext.openFileInput(FILE_RECORDS);
            ObjectInputStream ois = new ObjectInputStream(fis);
                conversations = (List<Conversation>) ois.readObject();
                ois.close();
        } catch (Exception e) {
            Log.e(TAG, "Load past conversations", e);
        } finally {
            if (conversations == null) {
                conversations = new ArrayList<Conversation>();
            } else {
                int size = conversations.size();
                if (size > 50) {
                    conversations = new ArrayList<Conversation>(conversations.subList(size -50, size));
                }
            }
        }
        Collections.reverse(conversations);
    }

    public void writeRecord() {
        try {
            FileOutputStream fos = сontext.openFileOutput(FILE_RECORDS, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(conversations);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            Log.d("Error Write file - ", e.toString());
            e.printStackTrace();
        }
    }

    public void addConversation(Conversation newConversation) {
        conversations.add(newConversation);
    }

    public List<Conversation> getConversations() {
        return conversations;
    }
}
