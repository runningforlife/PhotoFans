package com.github.runningforlife.photosniffer.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.presenter.FavorImagePresenter;
import com.github.runningforlife.photosniffer.presenter.FavorImagePresenterImpl;
import com.github.runningforlife.photosniffer.ui.FavorView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.ImageAdapterCallback;
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
        GridLayoutManager glm = new GridLayoutManager(getContext(),2);
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
        if(mRcvFavorList.getAdapter() == null){
            mRcvFavorList.setAdapter(mAdapter);
        }
        //mRcvFavorList.invalidate();
        //FIXME: D/skia: --- decoder->decode returned false
        mAdapter.notifyDataSetChanged();
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
        String myFavorite = getString(R.string.my_favorite_images) +
                "(" + mPresenter.getItemCount() + ")";
        getActivity().setTitle(myFavorite);
    }
}
