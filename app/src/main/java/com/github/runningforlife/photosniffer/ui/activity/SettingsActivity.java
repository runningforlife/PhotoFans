package com.github.runningforlife.photosniffer.ui.activity;

import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.github.runningforlife.photosniffer.ui.fragment.SettingsFragment;


/**
 * an activity to manage user settings
 */

public class SettingsActivity extends BaseActivity {
    private static final String TAG = "Settings";

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        android.app.FragmentManager fragmentMgr = getFragmentManager();
        fragmentMgr.beginTransaction()
                   .add(android.R.id.content, new SettingsFragment())
                   .commit();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        Log.v(TAG,"onConfigurationChanged()");
        // in Android 7.0, switch preference display error, so recreate a new fragment
        FragmentManager fragmentMgr = getFragmentManager();
        fragmentMgr.beginTransaction()
                   .replace(android.R.id.content, new SettingsFragment())
                   .commitAllowingStateLoss();
        fragmentMgr.executePendingTransactions();
    }
}
