package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.ImageRealm;

import com.github.runningforlife.photosniffer.presenter.WallpaperPresenter;
import com.github.runningforlife.photosniffer.presenter.WallpaperPresenterImpl;
import com.github.runningforlife.photosniffer.ui.WallpaperView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapterCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmObject;

/**
 * fragment to manager wallpaper
 */

public class WallPaperFragment extends BaseFragment
        implements GalleryAdapterCallback, WallpaperView{
    public static final String TAG = "WallpaperFragment";

    @BindView(R.id.refresh)
    SwipeRefreshLayout mSrlRefresh;
    @BindView(R.id.rcv_img_list)
    RecyclerView mRcvWallpaper;

    private GalleryAdapter mAdapter;
    private WallpaperPresenter mPresenter;

    public static WallPaperFragment newInstance(){
        return new WallPaperFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){

        View root = inflater.inflate(R.layout.fragment_user_image, parent, false);

        ButterKnife.bind(this, root);

        initView();

        initPresenter();

        return root;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        Log.v(TAG,"onAttach()");
        try {
            mCallback = (FragmentCallback)context;
        }catch (ClassCastException e){
            Log.e(TAG,"parent activity must implement FragmentCallback");
            throw new IllegalStateException("refresh callback must be implemented");
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(mCallback != null){
            mCallback.onFragmentAttached();
        }

        mPresenter.onStart();

        setTitle();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mPresenter.onDestroy();
    }

    @Override
    public int getCount() {
        return mPresenter.getItemCount();
    }

    @Override
    public RealmObject getItemAtPos(int pos) {
        return mPresenter.getItemAtPos(pos);
    }

    @Override
    public void onItemClicked(View view, int pos, String adapter) {
        Log.v(TAG,"onItemClicked(): pos = " + pos);
        if(mCallback != null){
            mCallback.onItemClick(view,pos, ((ImageRealm)mPresenter.getItemAtPos(pos)).getUrl());
        }
    }

    @Override
    public void removeItemAtPos(int pos) {
        mPresenter.removeItemAtPos(pos);
    }

    @Override
    public void onDataSetRangeChange(int start, int count) {
        Log.v(TAG,"onDataSetRangeChange()");
        if(mRcvWallpaper.getAdapter() == null){
            mRcvWallpaper.setAdapter(mAdapter);
        }
        if(start == 0 && count > 0){
            mAdapter.notifyItemRangeInserted(start, count);
        }else if(start >= 0 && count < 0){
            mAdapter.notifyItemRangeRemoved(start, (-1)*count);
        }else{
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefreshDone(boolean isSuccess) {
        if(mSrlRefresh.isRefreshing()){
            mSrlRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onImageSaveDone(String path) {
        if(!TextUtils.isEmpty(path)){
            mCallback.showToast(getString(R.string.save_image_Success));
        }else{
            mCallback.showToast(getString(R.string.save_image_fail));
        }
    }

    @Override
    public void onWallpaperSetDone(boolean isOk) {
        if(isOk){
            mCallback.showToast(getString(R.string.set_wallpaper_success));
        }else{
            mCallback.showToast(getString(R.string.set_wallpaper_fail));
        }
    }

    @Override
    public void onContextMenuCreated(int pos, String adapter) {
        Log.v(TAG,"onContextMenuCreated()");

        mCurrentPos = pos;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        Log.v(TAG,"onContextItemSelected()");

        switch (item.getItemId()){
            case R.id.menu_save:
                mPresenter.saveImageAtPos(mCurrentPos);
                break;
            case R.id.menu_delete:
                mPresenter.removeItemAtPos(mCurrentPos);
                break;
            case R.id.menu_wallpaper:
                mPresenter.setWallpaperAtPos(mCurrentPos);
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    private void initView(){
        StaggeredGridLayoutManager sgm = new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL);
        mRcvWallpaper.setLayoutManager(sgm);

        mAdapter = new GalleryAdapter(getActivity(), this);
        mRcvWallpaper.setAdapter(mAdapter);
        mRcvWallpaper.setBackgroundResource(R.color.colorLightGrey);

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
        mPresenter = new WallpaperPresenterImpl(getContext());
        mPresenter.setView(this);
        mPresenter.init();
    }

    private void setTitle(){
        String title = getString(R.string.set_wallpaper);
        Activity activity = getActivity();
        if(activity != null) {
            activity.setTitle(title);
        }
    }
}
