package com.brightpattern.chatdemo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;

import com.brightpattern.api.chat.events.ChatVisualEvent;
import com.brightpattern.chatdemo.utils.AttachmentUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ChatListAdapter extends BaseAdapter {

    private List<ChatVisualEvent> listData;

    private Context context;

    public ChatListAdapter(Context context, List<ChatVisualEvent> listData) {
        this.listData = listData;
        this.context = context;
    }

    private final static int DURATION = 400;

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {

        ChatVisualEvent currentMessage = listData.get(position);
        DateFormat df = SimpleDateFormat.getDateTimeInstance();

        if (currentMessage.getType() == ChatVisualEvent.Type.TYPING_EVENT) {
            Log.d("TYPING","");
            view = LayoutInflater.from(context).inflate(R.layout.snippet_inbound_typing, parent, false);

            BitmapDrawable frame0 = (BitmapDrawable) context.getResources().getDrawable(R.drawable.step0);
            BitmapDrawable frame1 = (BitmapDrawable) context.getResources().getDrawable(R.drawable.step1);
            BitmapDrawable frame2 = (BitmapDrawable) context.getResources().getDrawable(R.drawable.step2);
            BitmapDrawable frame3 = (BitmapDrawable) context.getResources().getDrawable(R.drawable.step3);

            AnimationDrawable animation = new AnimationDrawable();
            animation.setOneShot(false);
            animation.addFrame(frame0, DURATION);
            animation.addFrame(frame1, DURATION);
            animation.addFrame(frame2, DURATION);
            animation.addFrame(frame3, DURATION);

            ImageView typingImage = (ImageView) view.findViewById(R.id.typingImage);
            typingImage.setBackground(animation);

            if (!animation.isRunning()) {
                animation.setVisible(true, true);
                animation.start();
            }

            TextView pubDate = (TextView) view.findViewById(R.id.pub_date);
            pubDate.setText("");

            TextView sender = (TextView) view.findViewById(R.id.senderName);
            sender.setText(currentMessage.getPartyName());

            return view;
        }

        if (currentMessage.getType() == ChatVisualEvent.Type.PARTY_EVENT) {
            view = LayoutInflater.from(context).inflate(R.layout.snippet_party_list_row, parent, false);
            TextView partyMessage = (TextView) view.findViewById(R.id.partyMessage);
            if (currentMessage.getPartyEventType() == ChatVisualEvent.PartyEvent.JOINED) {
                partyMessage.setText(currentMessage.getPartyName() + " has joined the chat");
            } else {
                partyMessage.setText(currentMessage.getPartyName() + " has left the chat");
            }
        } else {
            if (currentMessage.getType() == ChatVisualEvent.Type.TEXT_MESSAGE) {
                if (currentMessage.getDirection() == ChatVisualEvent.Direction.INBOUND) {
                    view = LayoutInflater.from(context).inflate(R.layout.snippet_inbound_text_list_row, parent, false);
                } else {
                    view = LayoutInflater.from(context).inflate(R.layout.snippet_outbound_text_list_row, parent, false);
                }
                TextView message = (TextView) view.findViewById(R.id.message);
                message.setText(currentMessage.getText());
            } else if (currentMessage.getType() == ChatVisualEvent.Type.FILE_MESSAGE) {
                if (currentMessage.getDirection() == ChatVisualEvent.Direction.INBOUND) {
                    view = LayoutInflater.from(context).inflate(R.layout.snippet_inbound_file_list_row, parent, false);
                } else {
                    view = LayoutInflater.from(context).inflate(R.layout.snippet_outbound_file_list_row, parent, false);
                }
                Bitmap image = null;
                if (currentMessage.getFileId() != null) {
                    String imageFilePath = AttachmentUtils.getFileName(currentMessage.getFileId());
                    image = BitmapFactory.decodeFile(new File(context.getFilesDir(), imageFilePath).getPath());
                }
                if (image != null) {
                    ImageView imageView = (ImageView) view.findViewById(R.id.attachmentImage);
                    imageView.setImageBitmap(image);
                }
            }

            TextView sender = (TextView) view.findViewById(R.id.senderName);
            if (currentMessage.getDirection() == ChatVisualEvent.Direction.INBOUND) {
                if (currentMessage.getPartyName() == null || currentMessage.getPartyName().isEmpty()) {
                    sender.setVisibility(View.INVISIBLE);
                } else {
                    sender.setText(currentMessage.getPartyName());
                }
            } else {
                sender.setText(R.string.chat_sender_me);
            }
        }
        TextView pubDate = (TextView) view.findViewById(R.id.pub_date);
        pubDate.setText(df.format(currentMessage.getDate()));
        return view;
    }
}