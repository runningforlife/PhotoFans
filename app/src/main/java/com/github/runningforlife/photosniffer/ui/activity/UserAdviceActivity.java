package com.github.runningforlife.photosniffer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by jason on 1/20/18.
 */

public class UserAdviceActivity extends BaseActivity {
    private static final String TAG = "UserAdviceActivity";

    @BindView(R.id.et_email) EditText mEtEmail;
    @BindView(R.id.et_advice) EditText mEtAdvice;
    @BindView(R.id.btn_send) Button mBtnSend;

    private boolean mIsSent;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_user_advice);

        ButterKnife.bind(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!mIsSent) {
            saveUserAdvice(mEtEmail.getText().toString(), mEtAdvice.getText().toString());
        }
    }

    @OnClick(R.id.btn_send)
    public void sendAdvice() {
        mIsSent = true;
        saveUserAdvice(mEtEmail.getText().toString(), mEtAdvice.getText().toString());
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

        cloud.saveAdvice(email, advice, new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e != null) {
                    Log.d(TAG, "uploadAdviceToCloud: done:" + e);
                }
                if (mIsSent) {
                    navigateToParentActivity();
                }
            }
        });
    }
}
