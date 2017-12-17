package com.github.runningforlife.photosniffer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.AdapterView;

import com.github.runningforlife.photosniffer.R;

/**
 * a common used activity class
 */

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    static final String PATH_NAME = "photos";
    static final String PATH_CRASH_LOG = "log";

    @Override
    protected void onCreate(Bundle saveState){
        super.onCreate(saveState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
