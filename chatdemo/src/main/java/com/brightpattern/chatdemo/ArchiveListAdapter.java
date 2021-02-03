package com.brightpattern.chatdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ArchiveListAdapter extends BaseAdapter {

    private List<Conversation> listData;
    private Context context;

    public ArchiveListAdapter(Context context, List<Conversation> listData) {
        this.listData = listData;
        this.context = context;
    }

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

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Conversation conversation = listData.get(position);

        view = LayoutInflater.from(context).inflate(R.layout.snippet_conversation_list_row, parent, false);
        TextView dateConversation = (TextView) view.findViewById(R.id.dateConversation);

        DateFormat df = SimpleDateFormat.getDateTimeInstance();

        dateConversation.setText(df.format(conversation.getDate()));
        return view;
    }
}
