package com.github.runningforlife.photosniffer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;

/**
 * a common used activity class
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateToParentActivity();
        }

        return super.onOptionsItemSelected(item);
    }


    protected void navigateToParentActivity() {
        // child should implement this
        onBackPressed();
    }

}
