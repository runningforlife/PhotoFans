package com.github.runningforlife.photofans.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.ui.fragment.AllPicturesFragment;

public class GalleryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AllPicturesFragment.RefreshCallback {

    private static final String TAG = "GalleryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startSetting();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favorite) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_category) {

        } else if (id == R.id.nav_manage) {
            startSetting();
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    private void initView(){

        FragmentManager fragmentMgr = getSupportFragmentManager();
        Fragment fragment = fragmentMgr.findFragmentByTag(AllPicturesFragment.TAG);
        if(fragment == null){
            fragment = AllPicturesFragment.newInstance();
            fragmentMgr.beginTransaction()
                    .add(R.id.fragment_container,fragment,AllPicturesFragment.TAG)
                    .commit();
        }

    }

    private void startSetting(){
        setRefreshing(false);

        Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
        startActivity(intent);
    }

    private void setRefreshing(boolean refreshing){
        AllPicturesFragment fragment = (AllPicturesFragment)getSupportFragmentManager().
                findFragmentByTag(AllPicturesFragment.TAG);
        if(fragment != null && fragment.isRefreshing()){
            fragment.setRefreshing(refreshing);
        }
    }
}
