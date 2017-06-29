package com.github.runningforlife.photosniffer.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.ui.fragment.AllPicturesFragment;
import com.github.runningforlife.photosniffer.ui.fragment.BaseFragment;
import com.github.runningforlife.photosniffer.ui.fragment.FavoriteImageFragment;
import com.github.runningforlife.photosniffer.ui.fragment.FullScreenImageFragment;
import com.github.runningforlife.photosniffer.ui.fragment.WallPaperFragment;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AllPicturesFragment.RefreshCallback,ActivityCompat.OnRequestPermissionsResultCallback,
        BaseFragment.ItemClickListener{

    private static final String TAG = "GalleryActivity";
    final static int MY_STORAGE_PERMISSION_REQUEST = 100;

    @BindView(R.id.toolbar) Toolbar toolbar;

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
    public void onItemClick(int pos, String url) {
        Log.v(TAG,"onItemClick(): pos = " + pos);
        if(!TextUtils.isEmpty(url)) {
            ActionBar toolbar = getSupportActionBar();
            if(toolbar != null) {
                toolbar.hide();
            }

            showFullscreenFragment(url);
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

    @Override
    public void onRefreshDone(boolean isSuccess) {
        Log.v(TAG,"onRefreshDone()");

        if(isSuccess) {
            Toast.makeText(getApplicationContext(), R.string.refresh_success, Toast.LENGTH_SHORT)
                    .show();
        }else{
            Toast.makeText(getApplicationContext(),R.string.refresh_error,Toast.LENGTH_SHORT)
                    .show();
        }
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
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    private void initView(){

        FragmentManager fragmentMgr = getSupportFragmentManager();
        Fragment fragment = fragmentMgr.findFragmentByTag(AllPicturesFragment.TAG);
        if(fragment == null){
            fragment = AllPicturesFragment.newInstance();
        }

        fragmentMgr.beginTransaction()
                .replace(R.id.fragment_container,fragment,AllPicturesFragment.TAG)
                .commit();
    }

    private void startSetting(){
        setRefreshing(false);

        Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
        startActivity(intent);
    }

    private void setRefreshing(boolean refreshing){
        AllPicturesFragment fragment = (AllPicturesFragment)getSupportFragmentManager().
                findFragmentByTag(AllPicturesFragment.TAG);
        if(fragment != null && fragment.isRefreshing() && fragment.isVisible()){
            fragment.setRefreshing(refreshing);
        }

        FavoriteImageFragment fragment1 = (FavoriteImageFragment)getSupportFragmentManager()
                .findFragmentByTag(FavoriteImageFragment.TAG);
        if(fragment1 != null && fragment1.isVisible()){
            fragment1.setRefreshing(refreshing);
        }
    }

    private void startFavorFragment(){
        FragmentManager fragmentMgr = getSupportFragmentManager();

        FavoriteImageFragment fragment = (FavoriteImageFragment) fragmentMgr.findFragmentByTag(FavoriteImageFragment.TAG);

        if(fragment == null){
            fragment = FavoriteImageFragment.newInstance();
        }

        fragmentMgr.beginTransaction()
                .replace(R.id.fragment_container,fragment, FavoriteImageFragment.TAG)
                .commit();

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
        ft.replace(R.id.fragment_container, fragment, WallPaperFragment.TAG)
                .commit();
    }


    private void showFullscreenFragment(String url){
        FragmentManager fragmentMgr = getSupportFragmentManager();
        FullScreenImageFragment fragment = (FullScreenImageFragment)
                fragmentMgr.findFragmentByTag(FullScreenImageFragment.TAG);

        if(fragment == null){
            fragment = FullScreenImageFragment.newInstance(url);
        }

        fragmentMgr.beginTransaction()
                .replace(R.id.fragment_container, fragment, FullScreenImageFragment.TAG)
                .addToBackStack("FullScreenImage")
                .commit();
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
