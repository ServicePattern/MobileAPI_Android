package com.brightpattern.chatdemo;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.Secure;

public class SettingsActivity extends ActionBarActivity {

    private EditText editFirstName;
    private EditText editLastName;
    private EditText editPhoneNumber;

    private EditText editAppID;
    private EditText editServerAddress;
    private EditText editTenant;

    private ActionBar actionBar;

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        actionBar = getActionBar();

        editFirstName = (EditText)findViewById(R.id.editFirstName);
        editLastName = (EditText)findViewById(R.id.editLastName);
        editPhoneNumber = (EditText)findViewById(R.id.editPhoneNumber);
        editAppID = (EditText)findViewById(R.id.editAppID);
        editServerAddress = (EditText)findViewById(R.id.editServerAddress);
        editTenant = (EditText)findViewById(R.id.editTenant);

        loadSettings();
    }

    public void loadSettings() {
        settings = Settings.load(this);
        editFirstName.setText(settings.getFirstName());
        editLastName.setText(settings.getLastName());
        editPhoneNumber.setText(settings.getPhoneNumber());
        editAppID.setText(settings.getAppID());
        editServerAddress.setText(settings.getServerAddress());
        editTenant.setText(settings.getTenant());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_save_settigs:
                settings.setFirstName(editFirstName.getText().toString().trim());
                settings.setLastName(editLastName.getText().toString().trim());
                settings.setPhoneName(editPhoneNumber.getText().toString().trim());
                settings.setAppID(editAppID.getText().toString().trim());
                settings.setServerAddress(editServerAddress.getText().toString().trim());
                settings.setTenant(editTenant.getText().toString().trim());
                settings.save(this);

                ((DemoApplication)getApplication()).initChatWrapper();

                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.action_cancel_settigs:
                NavUtils.navigateUpFromSameTask(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
