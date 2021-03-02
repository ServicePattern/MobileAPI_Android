package com.brightpattern.chatdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class ArchiveActivity extends AppCompatActivity {

    private static final String TAG = ArchiveActivity.class.getSimpleName();

    private ListView archiveList;

    private ArchiveListAdapter adapter;

    private List<Conversation> archiveConversations = new ArrayList<Conversation>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        ConversationsAdapter cAdapter = new ConversationsAdapter(this);
        archiveConversations = cAdapter.getConversations();

        adapter = new ArchiveListAdapter(this, archiveConversations);
        archiveList = (ListView)findViewById(R.id.archiveListView);
        archiveList.setAdapter(adapter);

        archiveList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Conversation conversation = archiveConversations.get(position);

                Intent intent = new Intent(v.getContext(), ArchiveChatActivity.class);
                intent.putExtra(ArchiveChatActivity.CONVERSATION, conversation);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.archive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
