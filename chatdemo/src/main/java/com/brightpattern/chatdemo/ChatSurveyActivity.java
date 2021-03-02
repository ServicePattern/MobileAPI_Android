package com.brightpattern.chatdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.SeekBar;

import com.brightpattern.api.data.ShowFormData;
import com.brightpattern.api.data.ShowFormResult;


public class ChatSurveyActivity extends AppCompatActivity {

    private static final String TAG = ChatSurveyActivity.class.getSimpleName();

    public static final String SHOW_FORM =  TAG +".showForm";

    private SeekBar seekBarHelpful;
    private SeekBar seekBarRecommend;
    private Switch switchYN;

    private ShowFormData showFormData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_survey);

        switchYN = (Switch)findViewById(R.id.switchYN);
        seekBarHelpful = (SeekBar)findViewById(R.id.seekBarHelpful);
        seekBarRecommend = (SeekBar)findViewById(R.id.seekBarRecommend);
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchYN.setChecked(true);
        seekBarHelpful.setProgress(5);
        seekBarRecommend.setProgress(5);

        showFormData = (ShowFormData) getIntent().getSerializableExtra(SHOW_FORM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_survey, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.action_send_survey:
            case R.id.action_cancel_survey:
                Intent intent = new Intent(this, ChatActivity.class);
                if (item.getItemId() == R.id.action_send_survey) {
                    ShowFormResult result = new ShowFormResult();
                    result.setId(showFormData.getRequestId());
                    result.setName(showFormData.getName());
                    result.setResultValue("conversation_rating", String.valueOf(seekBarHelpful.getProgress()));
                    result.setResultValue("recommendation_probability", String.valueOf(seekBarRecommend.getProgress()));
                    result.setResultValue("issue_resolved", switchYN.isChecked()?"1":"0");
                    intent.putExtra(ChatActivity.FORM_RESULT, result);
                }
                NavUtils.navigateUpTo(this, intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
