package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.presenter.FavorImagePresenter;
import com.github.runningforlife.photosniffer.presenter.FavorImagePresenterImpl;
import com.github.runningforlife.photosniffer.ui.FavorPictureView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapterCallback;
import com.github.runningforlife.photosniffer.ui.anim.ScaleInOutItemAnimator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmObject;

/**
 * a fragment containing all favorite images
 */

public class FavoriteImageFragment extends BaseFragment
        implements GalleryAdapterCallback, FavorPictureView {
    public static final String TAG = "FavorImageFragment";
    @BindView(R.id.rcv_img_list) RecyclerView mRcvFavorList;
    @BindView(R.id.refresh) SwipeRefreshLayout mSrlRefresh;
    GalleryAdapter mAdapter;
    private FavorImagePresenter mPresenter;

    private List<String> mUserActionList;
    private int mCurrentPos;


    public static FavoriteImageFragment newInstance(){
        return new FavoriteImageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        Log.v(TAG,"onCreateView()");

        View root = inflater.inflate(R.layout.fragment_user_image,parent,false);
        ButterKnife.bind(this,root);

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
        } catch (ClassCastException e){
            Log.e(TAG,"parent activity must implement FragmentCallback");
            throw new IllegalStateException("parent activity must implement FragmentCallback");
        }
    }


    @Override
    public void onResume(){
        super.onResume();

        if(mCallback != null) {
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

    private void initView(){
        GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
        glm.setAutoMeasureEnabled(true);
        mRcvFavorList.setHasFixedSize(true);
        mRcvFavorList.setLayoutManager(glm);
        mRcvFavorList.setItemAnimator(new ScaleInOutItemAnimator());
        // restore back ground
        mRcvFavorList.setBackgroundResource(R.color.colorLightGrey);

        mAdapter = new GalleryAdapter(getActivity(),this);
        mAdapter.setImageLoader(Loader.GLIDE);
        mRcvFavorList.setAdapter(mAdapter);

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

        //option menu
        setHasOptionsMenu(true);
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
    public void onDataSetChanged() {
        Log.v(TAG,"onDataSetChanged()");
        //FIXME: D/skia: --- decoder->decode returned false
        mRcvFavorList.invalidate();
        mAdapter.notifyDataSetChanged();
    }

    //FIXME: image view is loaded too slow, we have to
    // refresh it manually
    @Override
    public void onRefreshDone(boolean isSuccess) {
        Log.v(TAG,"onRefreshDone()");

        if(mSrlRefresh.isRefreshing()) {
            mSrlRefresh.setRefreshing(false);
        }
    }

    @Override
    public boolean isRefreshing(){
        return mSrlRefresh.isRefreshing();
    }

    @Override
    public void setRefreshing(boolean enable){
        if(mSrlRefresh.isRefreshing()) {
            mSrlRefresh.setRefreshing(enable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.grid_view:
                GridLayoutManager glm = new GridLayoutManager(getContext(),2);
                mRcvFavorList.setLayoutManager(glm);
                glm.setAutoMeasureEnabled(true);
                return true;
            case R.id.list_view:
                LinearLayoutManager ll = new LinearLayoutManager(getContext());
                mRcvFavorList.setLayoutManager(ll);
                ll.setAutoMeasureEnabled(true);
                return true;
            case R.id.stagger_view:
                StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mRcvFavorList.setLayoutManager(sglm);
                sglm.setAutoMeasureEnabled(true);
                return true;
        }

        mRcvFavorList.removeAllViews();
        mAdapter.notifyDataSetChanged();

        return super.onOptionsItemSelected(item);
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

    private void setTitle(){
        String myFavorite = getString(R.string.my_favorite_images);
        Activity activity = getActivity();
        if(activity != null){
            activity.setTitle(myFavorite);
        }
    }

    private void initPresenter(){
        mPresenter = new FavorImagePresenterImpl(getContext(),this);
        mPresenter.init();
    }

}
