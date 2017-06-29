package com.github.runningforlife.photosniffer.ui.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

/**
 * a common used activity class
 */

public class BaseActivity extends AppCompatActivity {
    static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    static final String PATH_NAME = "photos";
    static final String PATH_CRASH_LOG = "log";

    @Override
    protected void onCreate(Bundle saveState){
        super.onCreate(saveState);
    }


}
