package com.brightpattern.chatdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.io.*;

import android.net.Uri;
import android.provider.MediaStore;
import android.database.Cursor;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.brightpattern.api.AsyncCallback;
import com.brightpattern.api.Chat;
import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.events.ChatEvent;
import com.brightpattern.api.data.ChatInfo;
import com.brightpattern.chatdemo.utils.AttachmentUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static int LOAD_FILE_RESULTS = 1;
    private static int CAMERA_IMAGE_RESULT = 2;
    private TextView pastTextBtn;
    private TextView helpTextBtn;
    private ImageButton addImgBtn;
    private EditText editText;
    private String filePath = null;
    private Chat chatWrapper;
    private ChatEventHandler chatHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Main onCreate");

        helpTextBtn = (TextView) findViewById(R.id.btnHelp);
        pastTextBtn = (TextView) findViewById(R.id.btnPast);
        editText = (EditText) findViewById(R.id.editMessage);
        addImgBtn = (ImageButton) findViewById(R.id.btnImage);
        pastTextBtn.setPaintFlags(helpTextBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        chatWrapper = ((DemoApplication) getApplication()).getChatWrapper();

        chatHandler = new ChatEventHandler() {
            @Override
            public void onEvent(ChatEvent event) {
                if (event.getType() == ChatEvent.Type.STATE_CHANGE) {
                    updateButtonsState();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Main onResume");

        chatWrapper.addChatEventHandler(chatHandler);

        if (chatWrapper.isInitiated()) {
            if (chatWrapper.getState() == ChatInfo.State.UNDEFINED) {
                helpTextBtn.setEnabled(false);
                chatWrapper.checkActiveChatWithHistory(new AsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean activeChat) {
                        updateButtonsState();
                        if (activeChat) {
                            startService(new Intent(MainActivity.this, ChatSessionService.class));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e(TAG, "Check active chat", t);
                        updateButtonsState();
                    }
                });
            } else {
                updateButtonsState();
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Please fill connection settings")
                    .setMessage("Fill settings now?")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        }
                    })
                    .setNegativeButton("Close", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatWrapper.removeChatEventHandler(chatHandler);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        Log.d(TAG, "onSave");
        state.putString("firstMessage", editText.getText().toString());
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        String msg = state.getString("firstMessage");
        if (msg != null) {
            editText.setText(msg);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            this.filePath = filePath;

            goChat(editText.getEditableText().toString(), this.filePath);
        } else if (requestCode == CAMERA_IMAGE_RESULT) {
            goChat(editText.getEditableText().toString(), filePath);
        }
    }

    public void addImage(View view) {

        final CharSequence[] options = {
                getResources().getText(R.string.action_take_photo),
                getResources().getText(R.string.action_choose_from_library),
                getResources().getText(R.string.action_cancel)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    public void goChat(View view) {
        goChat(editText.getText().toString(), null);
    }

    private void goChat(final String firstMessage, final String firstFilePath) {
        if (chatWrapper.isInitiated()) {
            if (!chatWrapper.isActiveState()) {
                chatWrapper.checkAvailability(new AsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        if (result) {
                            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                            if (!chatWrapper.isActiveState()) {
                                intent.putExtra(ChatActivity.CREATE_CHAT, true);
                                if (firstMessage != null && !firstMessage.isEmpty()) {
                                    intent.putExtra(ChatActivity.FIRST_MESSAGE, firstMessage);
                                }
                                if (firstFilePath != null && !firstFilePath.isEmpty()) {
                                    intent.putExtra(ChatActivity.FIRST_FILE, firstFilePath);
                                }
                            }
                            startActivity(intent);
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Request failed")
                                    .setMessage("Sorry, the services are not available now. Please try again later.")
                                    .setNegativeButton("Close", null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {

                    }
                });
            } else {
                startActivity(new Intent(this, ChatActivity.class));
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please, set connection settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateButtonsState() {
        helpTextBtn.setEnabled(true);
        if (chatWrapper.isActiveState()) {
            editText.setEnabled(false);
            editText.setText("");
            helpTextBtn.setText(R.string.label_connected);
            addImgBtn.setVisibility(View.INVISIBLE);
        } else {
            editText.setEnabled(true);
            helpTextBtn.setText(R.string.label_help_me);
            addImgBtn.setVisibility(View.VISIBLE);
        }
    }

    public void goPast(View view) {
        Intent intent = new Intent(this, ArchiveActivity.class);
        startActivity(intent);
    }
}
