package com.github.runningforlife.photosniffer.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.presenter.ImageType;
import com.github.runningforlife.photosniffer.ui.GalleryView;
import com.github.runningforlife.photosniffer.ui.fragment.AllPicturesFragment;
import com.github.runningforlife.photosniffer.ui.fragment.BaseFragment;
import com.github.runningforlife.photosniffer.ui.fragment.FavoriteImageFragment;
import com.github.runningforlife.photosniffer.ui.fragment.FullScreenImageFragment;
import com.github.runningforlife.photosniffer.ui.fragment.ImageDetailPagerFragment;
import com.github.runningforlife.photosniffer.ui.fragment.RetrieveHintFragment;
import com.github.runningforlife.photosniffer.ui.fragment.WallPaperFragment;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.ToastUtil;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_FAVOR;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_GALLERY;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_WALLPAPER;

public class GalleryActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback, BaseFragment.FragmentCallback, GalleryView {

    private static final String TAG = "GalleryActivity";
    final static int MY_STORAGE_PERMISSION_REQUEST = 100;

    private boolean mHintFragmentAdded;
    private FragmentManager mFragmentMgr;
    private ImageDetailPagerFragment mImagePagerFragment;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        //headerView = navView.getHeaderView(0);

        mHintFragmentAdded = false;
        mFragmentMgr = getSupportFragmentManager();

        initView();

        mDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG,"onResume()");
        checkStoragePermission();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mImagePagerFragment != null && mImagePagerFragment.isVisible()) {
            mImagePagerFragment.runExitAnimation(new Runnable() {
                @Override
                public void run() {
                    if (mFragmentMgr.getBackStackEntryCount() > 0) {
                        mFragmentMgr.popBackStack();
                    }
                }
            });
            // restore home icon
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.setToolbarNavigationClickListener(null);
            mDrawerToggle.syncState();
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
        switch (id){
            case R.id.nav_favorite:
                startFragmentByTag(FavoriteImageFragment.TAG);
                break;
            case R.id.nav_gallery:
                startFragmentByTag(AllPicturesFragment.TAG);
                break;
            case R.id.nav_wallpaper:
                startFragmentByTag(WallPaperFragment.TAG);
                break;
            case R.id.nav_settings:
                startSetting();
                break;
            default:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(View sharedView, int pos, int type) {
        Log.v(TAG,"onItemClick(): pos = " + pos);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        showImagePagerFragment(sharedView, pos, type);
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
    }

    @Override
    public void onRefreshDone(boolean success) {
        Log.v(TAG,"onRefreshDone()");
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
        // show hint
        showImageRetrieveHint();
    }

    private void initView() {
        Log.v(TAG,"initView()");
        startFragmentByTag(AllPicturesFragment.TAG);
    }

    private void startSetting() {
        setRefreshing(false);
        Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
        startActivity(intent);
    }

    private void setRefreshing(boolean refreshing) {
        FragmentManager fragmentMgr = getSupportFragmentManager();
        Fragment fragment =  fragmentMgr.findFragmentById(R.id.fragment_container);
        if(fragment != null && fragment instanceof BaseFragment){
            ((BaseFragment)fragment).setRefreshing(refreshing);
        }
    }

    private void startFragmentByTag(String tag) {
        Log.v(TAG,"startFragmentByTag()");
        BaseFragment fragment = (BaseFragment) mFragmentMgr.findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case FavoriteImageFragment.TAG:
                    fragment = FavoriteImageFragment.newInstance();
                    break;
                case WallPaperFragment.TAG:
                    fragment = WallPaperFragment.newInstance();
                    break;
                default:
                    fragment = AllPicturesFragment.newInstance();
                    break;
            }
        }

        mFragmentMgr.beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag)
                    .commit();
    }

    private void showFullScreenImage(View sharedView, int pos, String url){
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra(FullScreenImageFragment.POSITION, pos);
        intent.putExtra(FullScreenImageFragment.IMAGE_URL, url);

        if (Build.VERSION.SDK_INT >= 16) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, sharedView,
                            getString(R.string.activity_image_transition) + String.valueOf(pos));
            startActivity(intent, options.toBundle());
        }
    }

    private void showImagePagerFragment(View iv, int pos, int type) {
        int[] screenLocation = new int[2];
        iv.getLocationOnScreen(screenLocation);
        mImagePagerFragment = ImageDetailPagerFragment.newInstance(
                pos, screenLocation, iv.getWidth(), iv.getHeight(), type);
        mFragmentMgr.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, mImagePagerFragment)
                    .commit();

    }

    private void showImageRetrieveHint() {
        Log.v(TAG,"showImageRetrieveHint(): isNewUser = ");
        FragmentManager fm = getSupportFragmentManager();

        RetrieveHintFragment fragment = (RetrieveHintFragment)
                fm.findFragmentByTag(RetrieveHintFragment.TAG);
        if (fragment == null && !mHintFragmentAdded) {
            fragment = (RetrieveHintFragment) RetrieveHintFragment.newInstance();

            FragmentTransaction ft = fm.beginTransaction();

            ft.setCustomAnimations(R.anim.anim_enter_from_top, android.R.anim.fade_out);
            ft.add(R.id.fragment_container, fragment, RetrieveHintFragment.TAG)
                    .commitAllowingStateLoss();

            int dismissCount = getResources().getInteger(R.integer.dialog_dismiss_count_down);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissRetrieveHintFragment();
                }
            }, dismissCount);
        }

    }

    private void dismissRetrieveHintFragment() {
        Log.v(TAG,"dismissRetrieveHintFragment()");

        FragmentManager fm = getSupportFragmentManager();
        RetrieveHintFragment rhf = (RetrieveHintFragment) fm.findFragmentByTag(RetrieveHintFragment.TAG);
        if(rhf != null) {
            rhf.dismiss();
            fm.beginTransaction()
                .remove(rhf)
                .commit();
        }
    }

    private void makeAppDir() {
        String appName = getString(R.string.app_name);
        String imgPath = ROOT_PATH + File.separator + appName + File.separator + PATH_NAME;
        File file = new File(imgPath);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    private void checkStoragePermission() {
        Log.v(TAG,"checkStoragePermission()");
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this,permissions, MY_STORAGE_PERMISSION_REQUEST);
        } else {
            makeAppDir();
        }
    }
}
