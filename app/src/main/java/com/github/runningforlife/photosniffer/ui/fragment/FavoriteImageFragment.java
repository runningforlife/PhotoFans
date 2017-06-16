package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
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
import com.github.runningforlife.photosniffer.ui.FavorView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.ImageAdapterCallback;
import com.github.runningforlife.photosniffer.ui.anim.ScaleInOutItemAnimator;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;
import com.github.runningforlife.photosniffer.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * a fragment containing all favorite images
 */

public class FavoriteImageFragment extends BaseFragment
        implements ImageAdapterCallback, FavorView{
    public static final String TAG = "FavorImageFragment";
    @BindView(R.id.rcv_favor) RecyclerView mRcvFavorList;
    @BindView(R.id.srl_favor_refresh) SwipeRefreshLayout mSrlRefresh;
    GalleryAdapter mAdapter;
    private FavorImagePresenter mPresenter;

    public static FavoriteImageFragment newInstance(){
        return new FavoriteImageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        Log.v(TAG,"onCreateView()");

        View root = inflater.inflate(R.layout.fragment_favor_image,parent,false);
        ButterKnife.bind(this,root);

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
    public void onDestroy(){
        super.onDestroy();

        mPresenter.onDestroy();
    }

    private void initView(){
        LinearLayoutManager ll = new LinearLayoutManager(getContext());
        ll.setAutoMeasureEnabled(true);
        mRcvFavorList.setHasFixedSize(true);
        mRcvFavorList.setLayoutManager(ll);
        mRcvFavorList.setItemAnimator(new ScaleInOutItemAnimator());

        mAdapter = new GalleryAdapter(getContext(),this);
/*        mAdapter.setImageWidth(IMAGE_WIDTH);
        mAdapter.setImageHeight(IMAGE_HEIGHT);*/
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

    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG,"onDataSetChanged()");
        //mRcvFavorList.invalidate();
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
        if(TextUtils.isEmpty(path)){
            ToastUtil.showToast(getContext(),getString(R.string.save_image_fail));
        }else{
            ToastUtil.showToast(getContext(),getString(R.string.save_image_Success) + path);
        }
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
