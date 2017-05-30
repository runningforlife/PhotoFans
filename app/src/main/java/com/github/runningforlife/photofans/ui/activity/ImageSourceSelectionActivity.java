package com.github.runningforlife.photofans.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.model.ImageWebSite;
import com.github.runningforlife.photofans.model.RealmManager;
import com.github.runningforlife.photofans.model.VisitedPageInfo;
import com.github.runningforlife.photofans.ui.adapter.MultiSelectionListAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

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
    public void onPause(){
        super.onPause();

        String key = getString(R.string.pref_choose_image_source);

        List<String> values = mAdapter.getImageSource();

        SharedPreferences.Editor editor = mSharePref.edit();
        editor.putStringSet(key,new HashSet<>(values));
        editor.apply();

        // update database
        saveImageSource(values);
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
        List<ImageWebSite> srcList = intent.getParcelableArrayListExtra("image_source");
        List<String> defaultSrc = intent.getStringArrayListExtra("default_value");

        mAdapter = new MultiSelectionListAdapter(srcList);
        mAdapter.setDefaultSource(defaultSrc);
        mAdapter.setCallback(this);
        mImageSrcList.setAdapter(mAdapter);
    }

    @Override
    public void onLongClick(ImageWebSite src) {
        Log.v(TAG,"onLongClick()");

        startBrowser(src.url);
    }

    private void startBrowser(String url){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void saveImageSource(List<String> src){
        Log.v(TAG,"saveImageSource()");
        RealmManager helper = RealmManager.getInstance();

        Realm realm = Realm.getDefaultInstance();
        RealmResults<VisitedPageInfo> pages = helper.getAllUnvisitedPages(realm);

        if(pages.size() > 0) {
            Set<String> allUrls = new HashSet<>();
            for (VisitedPageInfo p : pages) {
                allUrls.add(p.getUrl());
            }
            // whether we should update database
            for (String url : src) {
                if(!allUrls.contains(url)){
                    VisitedPageInfo pageInfo = new VisitedPageInfo(url);
                    helper.writeAsync(pageInfo);
                }
            }
        }else{
            // save it to data base
            for(String url : src){
                VisitedPageInfo pageInfo = new VisitedPageInfo(url);
                helper.writeAsync(pageInfo);
            }
        }

        realm.close();
    }
}
