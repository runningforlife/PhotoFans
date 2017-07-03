package com.github.runningforlife.photosniffer.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.UiThread;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.ui.fragment.AllPicturesFragment;
import com.github.runningforlife.photosniffer.ui.fragment.BaseFragment;
import com.github.runningforlife.photosniffer.ui.fragment.FavoriteImageFragment;
import com.github.runningforlife.photosniffer.ui.fragment.FullScreenImageFragment;
import com.github.runningforlife.photosniffer.ui.fragment.WallPaperFragment;
import com.github.runningforlife.photosniffer.utils.ToastUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback,
        BaseFragment.FragmentCallback {

    private static final String TAG = "GalleryActivity";
    final static int MY_STORAGE_PERMISSION_REQUEST = 100;

    @BindView(R.id.toolbar) Toolbar toolbar;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.v(TAG,"onDrawerOpened()");
                setRefreshing(false);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Log.v(TAG,"onDrawerClosed()");

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        initView();
    }

    @Override
    public void onResume(){
        super.onResume();

        checkStoragePermission();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favorite) {
            startFavorFragment();
        } else if (id == R.id.nav_gallery) {
            startGalleryFragment();
        } else if (id == R.id.nav_wallpaper) {
            startWallpaperFragment();
        } else if (id == R.id.nav_settings) {
            startSetting();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(View sharedView, int pos, String url) {
        Log.v(TAG,"onItemClick(): pos = " + pos);
        if(!TextUtils.isEmpty(url)) {
/*            ActionBar toolbar = getSupportActionBar();
            if(toolbar != null) {
                toolbar.setShowHideAnimationEnabled(true);
                toolbar.hide();
            }*/
            //showFullscreenFragment(sharedView, pos, url);
            showFullScreenImage(sharedView, pos, url);
        }else{
            Log.e(TAG,"onItemClick(): url is empty");
        }
    }

    @Override
    public void onFragmentAttached() {
        Log.v(TAG,"onFragmentAttached()");
        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null) {
            toolbar.show();
        }
    }

    @UiThread
    @Override
    public void showToast(final String msg) {
        Log.v(TAG,"showToast()");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(GalleryActivity.this, msg);
            }
        });
        //ToastUtil.showToast(this, toast);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_STORAGE_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeAppDir();
                } else {
                    Log.v(TAG,"fail to request storage permission");
                }
            }
        }
    }

    private void initView(){
        Log.v(TAG,"initView()");
        startGalleryFragment();
    }

    private void startSetting(){
        setRefreshing(false);

        Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
        startActivity(intent);
    }

    private void setRefreshing(boolean refreshing){
        FragmentManager fragmentMgr = getSupportFragmentManager();
        BaseFragment fragment = (BaseFragment) fragmentMgr.findFragmentById(R.id.fragment_container);
        if(fragment != null){
            fragment.setRefreshing(refreshing);
        }
    }

    private void startFavorFragment(){
        FragmentManager fragmentMgr = getSupportFragmentManager();

        FavoriteImageFragment fragment = (FavoriteImageFragment) fragmentMgr.findFragmentByTag(FavoriteImageFragment.TAG);

        if(fragment == null){
            fragment = FavoriteImageFragment.newInstance();
        }
        FragmentTransaction ft = fragmentMgr.beginTransaction()
                .replace(R.id.fragment_container,fragment, FavoriteImageFragment.TAG);
        if(Build.VERSION.SDK_INT >= 19) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        ft.commit();
    }

    private void startGalleryFragment(){
        FragmentManager fragmentMgr = getSupportFragmentManager();
        AllPicturesFragment fragment = (AllPicturesFragment)fragmentMgr.findFragmentByTag(AllPicturesFragment.TAG);
        if(fragment == null) {
            fragment = (AllPicturesFragment) AllPicturesFragment.newInstance();
        }
        fragmentMgr.beginTransaction()
                .replace(R.id.fragment_container,fragment, AllPicturesFragment.TAG)
                .commit();
    }

    private void startWallpaperFragment(){
        FragmentManager fragmentMgr = getSupportFragmentManager();
        WallPaperFragment fragment = (WallPaperFragment)fragmentMgr.findFragmentByTag(WallPaperFragment.TAG);
        if(fragment == null){
            fragment = WallPaperFragment.newInstance();
        }
        FragmentTransaction ft = fragmentMgr.beginTransaction();
        if(Build.VERSION.SDK_INT >= 19) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        ft.replace(R.id.fragment_container, fragment, WallPaperFragment.TAG)
                .commit();
    }

    private void showFullScreenImage(View sharedView, int pos, String url){
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra(FullScreenImageFragment.POSITION, pos);
        intent.putExtra(FullScreenImageFragment.IMAGE_URL, url);
        if(Build.VERSION.SDK_INT >= 16) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, sharedView,
                            getString(R.string.activity_image_transition) + String.valueOf(pos));
            startActivity(intent, options.toBundle());
        }else{
            startActivity(intent);
        }
    }

    private void makeAppDir(){
        String appName = getString(R.string.app_name);
        String imgPath = ROOT_PATH + File.separator + appName + File.separator + PATH_NAME;
        File file = new File(imgPath);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    private void checkStoragePermission(){
        Log.v(TAG,"checkStoragePermission()");
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this,permissions, MY_STORAGE_PERMISSION_REQUEST);
        }else{
            makeAppDir();
        }
    }
}
