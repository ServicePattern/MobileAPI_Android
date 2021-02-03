package com.brightpattern.chatdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.app.ActionBar;

import com.brightpattern.api.chat.events.ChatVisualEvent;
import com.brightpattern.chatdemo.utils.AttachmentUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class ArchiveChatActivity extends Activity {

    private static final String TAG = ArchiveChatActivity.class.getSimpleName();

    public static final String CONVERSATION = TAG + ".conversation";

    private ListView chatList;
    private ChatListAdapter adapter;
    private List<ChatVisualEvent> events = new ArrayList<ChatVisualEvent>();

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_chat);

        actionBar = getActionBar();

        adapter = new ChatListAdapter(this, events);
        chatList = (ListView)findViewById(R.id.listArchiveDetail);
        chatList.setAdapter(adapter);

        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                ChatVisualEvent itemMessage = events.get(position);
                String fileName = AttachmentUtils.getFileName(itemMessage.getFileId());
                Bitmap tImage = BitmapFactory.decodeFile(fileName);


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Conversation conversation = (Conversation) getIntent().getSerializableExtra(CONVERSATION);

        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        actionBar.setSubtitle(df.format(conversation.getDate()));

        events.clear();
        events.addAll(conversation.getMessages());

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.archive_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
