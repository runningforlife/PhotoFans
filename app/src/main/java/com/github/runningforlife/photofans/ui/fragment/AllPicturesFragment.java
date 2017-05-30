package com.github.runningforlife.photofans.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.presenter.GalleryPresenter;
import com.github.runningforlife.photofans.presenter.GalleryPresenterImpl;
import com.github.runningforlife.photofans.ui.GalleryView;
import com.github.runningforlife.photofans.ui.activity.ImageDetailActivity;
import com.github.runningforlife.photofans.ui.adapter.GalleryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * a fragment to display all pictures
 */

public class AllPicturesFragment extends BaseFragment implements GalleryView,
        GalleryAdapter.ItemSelectedCallback {
    public static final String TAG = "AllPicturesFragment";

    @BindView(R.id.rcv_gallery) RecyclerView mRvImgList;
    @BindView(R.id.srl_refresh) SwipeRefreshLayout mRefresher;
    private GalleryPresenter mPresenter;
    private GalleryAdapter mAdapter;
    private RefreshCallback mCallback;

    public interface RefreshCallback {
        void onRefreshDone(boolean success);
    }

    public static Fragment newInstance(){
        return new AllPicturesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        Log.v(TAG,"onCreateView()");
        View root = inflater.inflate(R.layout.fragment_photos_gallery,parent,false);
        ButterKnife.bind(this,root);

        // need to call it here for onStart is too late
        initPresenter();

        initView();

        return root;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Log.v(TAG,"onAttach()");
        //initView(context);
        if(!(context instanceof RefreshCallback)){
            throw new IllegalStateException("refresh callback must be implemented");
        }

        mCallback = (RefreshCallback)context;
    }

    @Override
    public void onResume(){
        super.onResume();

        mPresenter.onStart();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        Log.v(TAG,"onDestroyView()");
        mPresenter.onDestroy();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        //mPresenter.onDestroy();
    }

    @Override
    public boolean isRefreshing(){
        return mRefresher.isRefreshing();
    }

    @Override
    public void setRefreshing(boolean enable){
        mRefresher.setRefreshing(enable);
    }

    @Override
    public void notifyDataChanged() {
        Log.v(TAG,"notifyDataChanged()");
        if(mRvImgList.getAdapter() == null){
            mRvImgList.setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefreshDone(boolean isSuccess) {
        Log.v(TAG,"onRefreshDone()");

        if(mRefresher.isRefreshing()){
            mRefresher.setRefreshing(false);
        }

        mCallback.onRefreshDone(isSuccess);
    }

    @Override
    public void onItemClick(int pos) {
        Log.v(TAG,"onItemClick(): pos = " + pos);

        if(mRefresher.isRefreshing()){
            mRefresher.setRefreshing(false);
        }
        Intent intent = new Intent(getContext(),ImageDetailActivity.class);
        intent.putExtra("image",pos);
        startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return mPresenter.getItemCount();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mPresenter.getItemAtPos(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        mPresenter.removeItemAtPos(pos);
    }

    @Override
    public void saveImage(int pos, Bitmap bitmap) {
        Log.d(TAG,"saveImage(): pos = " + pos);
        mPresenter.saveImageAtPos(pos,bitmap);
    }

    private void initView(){
        Log.v(TAG,"initView()");

        //LinearLayoutManager llMgr = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        GridLayoutManager gridLayoutMgr = new GridLayoutManager(getContext(),2, GridLayoutManager.VERTICAL,false);
        mRvImgList.setLayoutManager(gridLayoutMgr);
        mRvImgList.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new GalleryAdapter(getContext(),this);
        //mRvImgList.setAdapter(mAdapter);
        mRefresher.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark);
        mRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.v(TAG,"onRefresh()");
                mPresenter.refresh();
            }
        });
    }

    private void initPresenter(){
        mPresenter = new GalleryPresenterImpl(getContext(),this);
        mPresenter.init();
    }
}
