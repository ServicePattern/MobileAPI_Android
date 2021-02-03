package com.brightpattern.chatdemo;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.lang.String;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;

import android.app.ActionBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import com.brightpattern.api.AsyncCallback;
import com.brightpattern.api.Chat;
import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.EventCreator;
import com.brightpattern.api.chat.events.ChatEvent;
import com.brightpattern.api.chat.events.ErrorEvent;
import com.brightpattern.api.chat.events.IsVisualEvent;
import com.brightpattern.api.chat.events.StateChangeEvent;
import com.brightpattern.api.data.ChatInfo;
import com.brightpattern.api.data.ChatParameters;
import com.brightpattern.api.data.ChatParty;
import com.brightpattern.api.data.ShowFormResult;
import com.brightpattern.api.chat.events.ChatVisualEvent;
import com.brightpattern.chatdemo.utils.AttachmentUtils;
import com.brightpattern.chatdemo.webrtc.WebRtcConnection;

public class ChatActivity extends ActionBarActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();

    public static final String FIRST_FILE =  TAG +".firstFile";

    public static final String FIRST_MESSAGE =  TAG +".firstMessage";

    public static final String CREATE_CHAT =  TAG +".createChat";

    public static final String FORM_RESULT =  TAG +".formResult";

    private ListView chatList;
    private ChatListAdapter adapter;
    private List<ChatVisualEvent> chatEvents;

    private EditText newMessageText;
    private Button sendButton;
    private ImageButton addImageButton;

    private ActionBar actionBar;

    private static int LOAD_FILE_RESULTS = 1;
    private static int CAMERA_IMAGE_RESULT = 2;

    private String filePath;
    private String videoFilePath;

    private Chat chatWrapper;

    private WebRtcConnection webRtcConnection;

    private ChatEventHandler chatHandler;

    private ServiceConnection serviceConnection;

    private ChatSessionService.ChatSessionServiceInterface chatSessionService;

    private Timer timer = new Timer();

    private boolean isChatUIVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatWrapper =  ((DemoApplication)getApplication()).getChatWrapper();

        webRtcConnection =  ((DemoApplication)getApplication()).getWebRtcConnection();

        if (chatWrapper.getState() == ChatInfo.State.UNDEFINED) {
            goMain(); //goto parent activity if chat isn't initialized
            return;
        }

        actionBar = getActionBar();

        actionBar.setTitle("Chat");

        newMessageText = (EditText)findViewById(R.id.inputText);
        newMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chatWrapper.sendStartTyping();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        sendButton = (Button)findViewById(R.id.btnSend);
        addImageButton = (ImageButton)findViewById(R.id.btnAdd);

        chatEvents = new ArrayList<ChatVisualEvent>();

        adapter = new ChatListAdapter(this, chatEvents);
        chatList = (ListView)findViewById(R.id.listView);
        chatList.setAdapter(adapter);

        chatList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                ChatVisualEvent itemMessage = chatEvents.get(position);
                String fileName = AttachmentUtils.getFileName(itemMessage.getFileId());
                Bitmap tImage = BitmapFactory.decodeFile(fileName);
            }
        });

        chatHandler = new ChatEventHandler() {
            @Override
            public void onEvent(ChatEvent event) {
                if (event instanceof IsVisualEvent) {
                    hideTypingIndicator();
                    ChatVisualEvent ve = ((IsVisualEvent)event).toVisualEvent();
                    chatEvents.add(ve);
                    adapter.notifyDataSetChanged();
                    chatList.setSelection(adapter.getCount() - 1);
                } else {
                    switch (event.getType()) {
                        case ERROR:
                            ErrorEvent errorEvent = event.cast();
                            Log.e(TAG, "Chat error", errorEvent.getException());
                            Toast.makeText(getApplicationContext(), "Error!" + errorEvent.getException().getMessage(), Toast.LENGTH_LONG).show();
                            break;
                        case STATE_CHANGE:
                            StateChangeEvent stateChangeEvent = event.cast();
                            applyStateChange(stateChangeEvent.getState());
                            break;

                    }
                }
                if (chatWrapper.getTypingChatParty() != null) {
                    showTypingIndicator(chatWrapper.getTypingChatParty());
                } else {
                    hideTypingIndicator();
                }
            }
        };

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                ChatSessionService.ChatSessionBinder binder = (ChatSessionService.ChatSessionBinder) service;
                chatSessionService = binder.getService();
                chatSessionService.setChatUIVisibility(isChatUIVisible);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                chatSessionService = null;
            }
        };
    }



    private void showTypingIndicator(ChatParty party) {
        if (chatEvents.isEmpty() || chatEvents.get(chatEvents.size() - 1).getType() != ChatVisualEvent.Type.TYPING_EVENT) {
            chatEvents.add(ChatVisualEvent.createTypingEvent(party));
            adapter.notifyDataSetChanged();
            chatList.setSelection(adapter.getCount() - 1);
        }
    }

    public void hideTypingIndicator() {
        if (!chatEvents.isEmpty()) {
            ChatVisualEvent ve = chatEvents.get(chatEvents.size() - 1);
            if (ve.getType() == ChatVisualEvent.Type.TYPING_EVENT) {
                Log.d(TAG, " --- Remove typing event");
                chatEvents.remove(chatEvents.size() - 1);
                adapter.notifyDataSetChanged();
                chatList.setSelection(adapter.getCount() - 1);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isChatUIVisible = true;
        Intent intent = new Intent(this, ChatSessionService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        applyStateChange(chatWrapper.getState());

        chatWrapper.addChatEventHandler(chatHandler);

        chatEvents.clear();

        Boolean newChatRequested = getIntent().getBooleanExtra(CREATE_CHAT, false);

        processFormResult();

        if (newChatRequested && !chatWrapper.isActiveState()) {
            final String msg = getIntent().getStringExtra(FIRST_MESSAGE);
            final String filePath = getIntent().getStringExtra(FIRST_FILE);
            applyStateChange(ChatInfo.State.CONNECTING);
            chatWrapper.startNewChat(createChatParameters(), new AsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean value) {
                    startService(new Intent(ChatActivity.this, ChatSessionService.class));
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            if (msg != null && !msg.isEmpty()) {
                                chatWrapper.sendMessage(msg);
                            }
                            if (filePath != null && !filePath.isEmpty()) {
                                sendFile(filePath);
                            }
                        }
                    };
                    timer.schedule(task, 1000);
                }

                @Override
                public void onFailure(Throwable t) {
                    applyStateChange(ChatInfo.State.DISCONNECTED);
                    showErrorMsg(t);
                }
            });
        } else if (chatWrapper.getState() != ChatInfo.State.UNDEFINED) {
            chatEvents.addAll(ChatVisualEvent.adapt(chatWrapper.getChatEvents()));
            if (chatWrapper.getTypingChatParty() != null) {
                showTypingIndicator(chatWrapper.getTypingChatParty());
            }
            adapter.notifyDataSetChanged();
            chatList.setSelection(adapter.getCount() - 1);
        }
    }

    private void processFormResult() {
        Serializable formResult = getIntent().getSerializableExtra(FORM_RESULT);
        if (formResult != null && chatWrapper.isActiveState()) {
            chatWrapper.sendFormResult((ShowFormResult) formResult);
        }
    }

    private ChatParameters createChatParameters() {
        ChatParameters chatParameters = new ChatParameters();
        Settings settings = Settings.load(this);
        chatParameters.setFirstName(settings.getFirstName());
        chatParameters.setlastName(settings.getLastName());
        chatParameters.setPhoneNumber(settings.getPhoneNumber());

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            chatParameters.setLocation(location.getLatitude(), location.getLongitude());
        }
        return chatParameters;
    }

    private void showErrorMsg(Throwable t) {
        Toast.makeText(getApplicationContext(), "Error!" + t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isChatUIVisible = false;
        if (chatSessionService != null) {
            chatSessionService.setChatUIVisibility(false);
            unbindService(serviceConnection);
        }
        chatWrapper.removeChatEventHandler(chatHandler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOAD_FILE_RESULTS && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            sendFile(filePath);

        } else if (requestCode == CAMERA_IMAGE_RESULT) {
            sendFile(filePath);
        }
    }

    private void sendFile(final String filePath) {
        chatWrapper.uploadFile(filePath, new AsyncCallback<String>() {

            @Override
            public void onSuccess(String fileId) {
                EventCreator.FileType fileType = EventCreator.FileType.ATTACHMENT;
                if (AttachmentUtils.isImage(filePath)) {
                    fileType = EventCreator.FileType.IMAGE;
                    AttachmentUtils.createPreview(getApplicationContext(), filePath, fileId, chatWrapper.getChatSessionId());
                }
                chatWrapper.sendFile(fileId, fileType);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Send image", t);
                showErrorMsg(t);
            }
        });
    }

    public void addImage(View view) {
        final CharSequence[] options = {
                getResources().getText(R.string.action_take_photo),
                getResources().getText(R.string.action_choose_from_library),
                getResources().getText(R.string.action_cancel)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = AttachmentUtils.createImageFile();
                            filePath = photoFile.getAbsolutePath();
                        } catch (IOException e) {
                            Log.e(TAG, "Create temp image file", e);
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, CAMERA_IMAGE_RESULT);
                        }
                    }
                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, LOAD_FILE_RESULTS);
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void sendMessage(View view) {
        Log.d(TAG, "new Message");

        String message = newMessageText.getText().toString();

        chatWrapper.sendStopTyping();
        if(message.length() > 0) {
            newMessageText.setText("");
            chatWrapper.sendMessage(message);
        }
    }

    public void applyStateChange(ChatInfo.State state) {
        switch (state) {
            case QUEUED:
                actionBar.setSubtitle("Waiting...");
                newMessageText.setEnabled(true);
                sendButton.setEnabled(true);
                addImageButton.setEnabled(true);
                break;
            case CONNECTING:
                actionBar.setSubtitle("Connecting...");
                newMessageText.setEnabled(true);
                sendButton.setEnabled(true);
                addImageButton.setEnabled(true);
                break;
            case CONNECTED:
                actionBar.setSubtitle("Connected");
                newMessageText.setEnabled(true);
                sendButton.setEnabled(true);
                addImageButton.setEnabled(true);
                break;
            case DISCONNECTED:
            default:
                newMessageText.setEnabled(false);
                sendButton.setEnabled(false);
                addImageButton.setEnabled(false);
                actionBar.setSubtitle("Not connected");
        }
        this.invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_atcivity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chat_cancel:
                chatWrapper.finishCurrentChat();
                goMain();
                break;
            case R.id.action_chat_end:
                hideTypingIndicator();
                chatWrapper.finishCurrentConversation();
                break;
            case R.id.action_chat_close:

                Conversation newConversation = new Conversation(chatEvents, new Date());
                ConversationsAdapter cAdapter = new ConversationsAdapter(this);
                cAdapter.addConversation(newConversation);
                cAdapter.writeRecord();

                chatWrapper.finishCurrentChat();
                goMain();
                break;
            case R.id.action_chat_call:
            case R.id.action_chat_video_call:
                Intent callIntent = new Intent(this, VideoCallActivity.class);
                callIntent.putExtra(VideoCallActivity.AUDIO_CALL, true);
                if (item.getItemId() == R.id.action_chat_video_call) {
                    callIntent.putExtra(VideoCallActivity.VIDEO_CALL, true);
                }
                startActivity(callIntent);
                break;
            case R.id.action_chat_return_to_call:
                startActivity(new Intent(this, VideoCallActivity.class));
                break;
            case android.R.id.home:
                
                hideTypingIndicator();

                this.finish();
                return true;
            default:
                break;
        }

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem cancelItem = menu.findItem(R.id.action_chat_cancel);
        MenuItem endItem = menu.findItem(R.id.action_chat_end);
        MenuItem closeItem = menu.findItem(R.id.action_chat_close);
        MenuItem returnToCallItem = menu.findItem(R.id.action_chat_return_to_call);
        MenuItem videoCallItem = menu.findItem(R.id.action_chat_video_call);
        MenuItem callItem = menu.findItem(R.id.action_chat_call);
        if (chatWrapper.getState() == ChatInfo.State.CONNECTING || chatWrapper.getState() == ChatInfo.State.QUEUED) {
            cancelItem.setVisible(true);
            closeItem.setVisible(false);
            endItem.setVisible(false);
        } else if (chatWrapper.getState() == ChatInfo.State.CONNECTED) {
            cancelItem.setVisible(false);
            endItem.setVisible(true);
            closeItem.setVisible(false);
        } else {
            cancelItem.setVisible(false);
            endItem.setVisible(false);
            closeItem.setVisible(true);
        }
        boolean isWebRtcConnectionActive = webRtcConnection.isActive();
        returnToCallItem.setVisible(isWebRtcConnectionActive);
        videoCallItem.setVisible(!isWebRtcConnectionActive);
        callItem.setVisible(!isWebRtcConnectionActive);

        return true;
    }

    public void goMain() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
