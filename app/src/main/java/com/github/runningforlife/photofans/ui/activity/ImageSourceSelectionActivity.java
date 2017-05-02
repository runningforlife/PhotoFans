package com.github.runningforlife.photofans.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.model.ImageSource;
import com.github.runningforlife.photofans.ui.adapter.MultiSelectionListAdapter;

import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * an activity to select image source website
 */

public class ImageSourceSelectionActivity extends AppCompatActivity
            implements MultiSelectionListAdapter.SelectionItemClickListener {
    private static final String TAG = "ImageSourceSelection";

    @BindView(R.id.lv_image_source) ListView mImageSrcList;

    private MultiSelectionListAdapter mAdapter;
    private SharedPreferences mSharePref;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }
        setContentView(R.layout.activity_image_source_selection);

        ButterKnife.bind(this);

        initView();

        mSharePref = PreferenceManager.getDefaultSharedPreferences(this);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        String key = getString(R.string.pref_choose_image_source);

        List<String> values = mAdapter.getImageSource();

        SharedPreferences.Editor editor = mSharePref.edit();
        editor.putStringSet(key,new HashSet<String>(values));
        editor.apply();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id == android.R.id.home){
            Intent intent = new Intent(this, SettingsActivity.class);
            NavUtils.navigateUpTo(this,intent);
        }

        return super.onOptionsItemSelected(item);
    }


    private void initView(){
        Log.v(TAG,"initView()");

        Intent intent = getIntent();
        List<ImageSource> srcList = intent.getParcelableArrayListExtra("image_source");
        List<String> defaultSrc = intent.getStringArrayListExtra("default_value");

        mAdapter = new MultiSelectionListAdapter(srcList);
        mAdapter.setDefaultSource(defaultSrc);
        mAdapter.setCallback(this);
        mImageSrcList.setAdapter(mAdapter);
    }

    @Override
    public void onLongClick(ImageSource src) {
        Log.v(TAG,"onLongClick()");
    }
}
