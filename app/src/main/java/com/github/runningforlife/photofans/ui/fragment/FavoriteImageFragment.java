package com.github.runningforlife.photofans.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.presenter.FavorImagePresenter;
import com.github.runningforlife.photofans.presenter.FavorImagePresenterImpl;
import com.github.runningforlife.photofans.ui.FavorView;
import com.github.runningforlife.photofans.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photofans.ui.adapter.ImageAdapterCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * a fragment containing all favorite images
 */

public class FavoriteImageFragment extends BaseFragment
        implements ImageAdapterCallback, FavorView{
    public static final String TAG = "FavorImageFragment";
    @BindView(R.id.rcv_favor) RecyclerView mRcvFavorList;
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

    private void initView(){
        GridLayoutManager glm = new GridLayoutManager(getContext(),3);
        mRcvFavorList.setLayoutManager(glm);

        mAdapter = new GalleryAdapter(getContext(),this);
        mRcvFavorList.setAdapter(mAdapter);
    }

    private void initPresenter(){
        mPresenter = new FavorImagePresenterImpl(getContext(),this);
        mPresenter.init();
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
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onImageSaveDone(String path) {

    }

    private void setTitle(){
        String myFavorite = getString(R.string.my_favorite_images) +
                "(" + mPresenter.getItemCount() + ")";
        getActivity().setTitle(myFavorite);
    }
}
