package com.github.runningforlife.photosniffer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.runningforlife.photosniffer.R;
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

    protected void navigateToParentActivity() {
        Intent intent = new Intent(this, GalleryActivity.class);
        NavUtils.navigateUpTo(this,intent);
    }

}
