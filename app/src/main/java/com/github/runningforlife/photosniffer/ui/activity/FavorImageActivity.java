package com.github.runningforlife.photosniffer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.ui.fragment.FavoriteImageFragment;

/**
 * user favorite activity
 */

public class FavorImageActivity  extends BaseActivity{

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        FavoriteImageFragment fragment = FavoriteImageFragment.newInstance();

        FragmentManager fragmentMgr = getSupportFragmentManager();
        fragmentMgr.beginTransaction()
                   .add(fragment,FavoriteImageFragment.TAG)
                   .commit();

        initToolbar();
    }

    private void initToolbar(){
        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null){
            toolbar.setDisplayHomeAsUpEnabled(true);
            toolbar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id == android.R.id.home){
            Intent intent = new Intent(this,GalleryActivity.class);
            NavUtils.navigateUpTo(this, intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
