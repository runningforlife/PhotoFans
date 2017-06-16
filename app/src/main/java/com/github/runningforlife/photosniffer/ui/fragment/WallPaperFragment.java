package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
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
        implements ImageAdapterCallback, WallpaperView{
    public static final String TAG = "WallpaperFragment";

    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout mSrlRefresh;
    @BindView(R.id.rcv_gallery)
    RecyclerView mRcvWallpaper;

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
    public void onWallpaperSetDone(boolean isOk) {

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
}
