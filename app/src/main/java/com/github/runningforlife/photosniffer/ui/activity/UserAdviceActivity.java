package com.github.runningforlife.photosniffer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.widget.EditText;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jason on 1/20/18.
 */

public class UserAdviceActivity extends BaseActivity {

    @BindView(R.id.et_email) EditText mEtEmail;
    @BindView(R.id.et_advice) EditText mEtAdvice;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_user_advice);

        ButterKnife.bind(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        saveUserAdvice(mEtEmail.getText().toString(), mEtAdvice.getText().toString());
    }

    @Override
    protected void navigateToParentActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        NavUtils.navigateUpTo(this, intent);
    }

    private void saveUserAdvice(String email, String data) {
        if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(data)) {
            if (MiscUtil.isConnected(this)) {
                uploadAdviceToCloud(email, data);
            } else {
                SharedPrefUtil.putString(getString(R.string.pref_report_issue_and_advice), email + ";" + data);
            }
        }
    }

    private void uploadAdviceToCloud(String email, String advice) {
        LeanCloudManager cloud = LeanCloudManager.getInstance();

        cloud.saveAdvice(email, advice);
    }
}
