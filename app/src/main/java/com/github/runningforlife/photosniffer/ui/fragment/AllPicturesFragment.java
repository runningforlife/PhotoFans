package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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
import com.github.runningforlife.photosniffer.presenter.GalleryPresenter;
import com.github.runningforlife.photosniffer.presenter.GalleryPresenterImpl;
import com.github.runningforlife.photosniffer.ui.GalleryView;
import com.github.runningforlife.photosniffer.ui.activity.ImageDetailActivity;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.ImageAdapterCallback;
import com.github.runningforlife.photosniffer.ui.anim.ScaleInOutItemAnimator;
import com.github.runningforlife.photosniffer.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * a fragment to display all pictures
 */

public class AllPicturesFragment extends BaseFragment implements GalleryView,
        ImageAdapterCallback {
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

        initView();
        // need to call it here for onStart is too late
        initPresenter();

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

        setTitle();
        // FIXME: some times, data set seems not loaded in recycle view
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
        mRvImgList.invalidate();
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
    public void onItemClicked(int pos,String adapter) {
        Log.v(TAG,"onItemClick(): pos = " + pos);

        if(mRefresher.isRefreshing()){
            mRefresher.setRefreshing(false);
        }
        Intent intent = new Intent(getContext(),ImageDetailActivity.class);
        intent.putExtra("image",pos);
        startActivity(intent);
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
    public int getCount() {
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

    private void saveImage(int pos, Bitmap bitmap) {
        Log.d(TAG,"saveImage(): pos = " + pos);
        mPresenter.saveImageAtPos(pos);
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone(): isOk = " + !TextUtils.isEmpty(path));
        if(!TextUtils.isEmpty(path)) {
            ToastUtil.showToast(getContext(), getString(R.string.save_image_Success) + path);
        }else{
            ToastUtil.showToast(getContext(),getString(R.string.save_image_fail));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.grid_view:
                GridLayoutManager glm = new GridLayoutManager(getContext(),2);
                mRvImgList.setLayoutManager(glm);
                glm.setAutoMeasureEnabled(true);
                return true;
            case R.id.list_view:
                LinearLayoutManager ll = new LinearLayoutManager(getContext());
                mRvImgList.setLayoutManager(ll);
                ll.setAutoMeasureEnabled(true);
                return true;
            case R.id.stagger_view:
                StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mRvImgList.setLayoutManager(sglm);
                sglm.setAutoMeasureEnabled(true);
                return true;
        }

        mAdapter.notifyDataSetChanged();

        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        Log.v(TAG,"initView()");

        //LinearLayoutManager llMgr = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        GridLayoutManager gridLayoutMgr = new GridLayoutManager(getContext(),2, GridLayoutManager.VERTICAL,false);
        mRvImgList.setHasFixedSize(true);
        mRvImgList.setLayoutManager(gridLayoutMgr);
        mRvImgList.setItemAnimator(new ScaleInOutItemAnimator());

        mAdapter = new GalleryAdapter(getContext(),this);
        mAdapter.setImageWidth(IMAGE_WIDTH);
        mAdapter.setImageHeight(IMAGE_HEIGHT);
        //adapter has to be set here, if not, refresh layout won't work
        mRvImgList.setAdapter(mAdapter);
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

        // option menu
        setHasOptionsMenu(true);
    }



    private void initPresenter(){
        mPresenter = new GalleryPresenterImpl(getContext(),this);
        mPresenter.init();
    }

    private void setTitle(){
        String appName = getString(R.string.app_name);
        Activity activity = getActivity();
        if(activity != null){
            activity.setTitle(appName);
        }
    }
}
