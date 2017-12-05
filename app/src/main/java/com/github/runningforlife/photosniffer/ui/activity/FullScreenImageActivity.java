package com.github.runningforlife.photosniffer.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.runningforlife.photosniffer.BuildConfig;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.ui.fragment.FullScreenImageFragment;

import butterknife.ButterKnife;

/**
 * an activity to show a full screen image
 */

public class FullScreenImageActivity extends BaseActivity {
    private static final String TAG = "FullScreenActivity";

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_fullscreen_image);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        initView();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                if(Build.VERSION.SDK_INT >= 21) {
                    finishAfterTransition();
                }else{
                    finish();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void initView(){
        FragmentManager fragmentMgr = getSupportFragmentManager();
        FullScreenImageFragment fragment = (FullScreenImageFragment) fragmentMgr.findFragmentByTag(FullScreenImageFragment.TAG);
        if(fragment == null){
            String url = getIntent().getStringExtra(FullScreenImageFragment.IMAGE_URL);
            fragment = FullScreenImageFragment.newInstance(url);
        }
        int pos = getIntent().getIntExtra(FullScreenImageFragment.POSITION,0);
        fragment.getArguments().putString(FullScreenImageFragment.POSITION, String.valueOf(pos));

        fragmentMgr.beginTransaction()
                .add(R.id.fullscreen_image_container, fragment, FullScreenImageFragment.TAG)
                .commit();
    }
}
