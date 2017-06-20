package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.presenter.WallpaperPresenter;
import com.github.runningforlife.photosniffer.presenter.WallpaperPresenterImpl;
import com.github.runningforlife.photosniffer.ui.WallpaperView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.ImageAdapterCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * fragment to manager wallpaper
 */

public class WallPaperFragment extends BaseFragment
        implements ImageAdapterCallback, WallpaperView, SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String TAG = "WallpaperFragment";
    public static final String ALARM_AUTO_WALLPAPER = "com.github.runningforlife.AUTO_WALLPAPER";

    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout mSrlRefresh;
    @BindView(R.id.rcv_gallery)
    RecyclerView mRcvWallpaper;
    private static WallpaperAlarmReceiver mAlarmReceiver;

    private GalleryAdapter mAdapter;
    private WallpaperPresenter mPresenter;

    public static WallPaperFragment newInstance(){
        return new WallPaperFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){

        View root = inflater.inflate(R.layout.fragment_photos_gallery, parent, false);

        ButterKnife.bind(this, root);

        initView();

        initPresenter();

        return root;
    }

    @Override
    public void onResume(){
        super.onResume();

        mPresenter.onStart();

        setTitle();

        registerPrefChangeListener();
    }

    @Override
    public int getCount() {
        return mPresenter.getItemCount();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mPresenter.getItemAtPos(pos);
    }

    @Override
    public void onItemClicked(int pos, String adapter) {

    }

    @Override
    public void onItemLongClicked(int pos, String adapter) {

    }

    @Override
    public void onImageLoadStart(int pos) {

    }

    @Override
    public void onImageLoadDone(int pos, boolean isSuccess) {

    }

    @Override
    public void removeItemAtPos(int pos) {
        mPresenter.removeItemAtPos(pos);
    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG,"onDataSetChanged()");
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onImageSaveDone(String path) {

    }

    private void initView(){
        StaggeredGridLayoutManager sgm = new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL);
        mRcvWallpaper.setLayoutManager(sgm);

        mAdapter = new GalleryAdapter(getContext(), this);
        mRcvWallpaper.setAdapter(mAdapter);

        mSrlRefresh.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark);
        mSrlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.v(TAG,"onRefresh()");
                mPresenter.refresh();
            }
        });
    }

    private void initPresenter(){
        mPresenter = new WallpaperPresenterImpl(getContext(), this);
        mPresenter.init();
    }

    private void setTitle(){
        String title = getString(R.string.set_wallpaper);
        Activity activity = getActivity();
        if(activity != null) {
            activity.setTitle(title);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String keyAutoWallpaper = getString(R.string.pref_automatic_wallpaper);
        boolean isAuto = sharedPreferences.getBoolean(keyAutoWallpaper, false);
        if(keyAutoWallpaper.equals(key)){
            if(isAuto) {
                registerAlarmReceiver();
                startAutoWallpaperAlarm();
            }else{
                cancelAutoWallpaperAlarm();
                unRegisterAlarmReceiver();
            }
        }
    }

    private void registerPrefChangeListener(){
        SharedPreferences sharePref = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        sharePref.registerOnSharedPreferenceChangeListener(this);
    }

    private void registerAlarmReceiver(){
        mAlarmReceiver = new WallpaperAlarmReceiver();
        getContext().registerReceiver(mAlarmReceiver,
                new IntentFilter(ALARM_AUTO_WALLPAPER));
    }

    private void unRegisterAlarmReceiver(){
        getContext().unregisterReceiver(mAlarmReceiver);
    }

    private void startAutoWallpaperAlarm(){
        // start a alarm to enable automatic wallpaper setting
        Intent intent = new Intent(ALARM_AUTO_WALLPAPER);
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10,6*1000*1000, pi);
    }

    private void cancelAutoWallpaperAlarm(){
        AlarmManager alarmMgr = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_AUTO_WALLPAPER);
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
    }


    private class WallpaperAlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG,"onReceive()");
            String action = intent.getAction();
            if(ALARM_AUTO_WALLPAPER.equals(action)){
                if(mPresenter == null){
                    mPresenter = new WallpaperPresenterImpl(getContext(),
                            WallPaperFragment.this);
                    mPresenter.init();
                    mPresenter.onStart();
                }

                mPresenter.setWallpaper();
            }
        }
    }
}
