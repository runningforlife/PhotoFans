package com.github.runningforlife.photosniffer.ui.fragment;

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
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.presenter.FavorImagePresenterImpl;
import com.github.runningforlife.photosniffer.presenter.RealmOp;
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

public class FavoriteImageFragment extends BaseFragment implements FavorPictureView {
    public static final String TAG = "FavorImageFragment";
    @BindView(R.id.rcv_gallery) RecyclerView mRcvFavorList;
    GalleryAdapter mAdapter;
    private FavorImagePresenterImpl mPresenter;

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
    public void onResume(){
        super.onResume();

        if(mCallback != null) {
            mCallback.onFragmentAttached();
        }

        setTitle(getString(R.string.my_favorite_images));
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
        mRcvFavorList.setAdapter(mAdapter);
        //option menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onItemClicked(View view, int pos, String adapter) {
        Log.v(TAG,"onItemClicked(): pos = " + pos);

        if(mCallback != null){
            mCallback.onItemClick(view,pos, ((ImageRealm)mPresenter.getItemAtPos(pos)).getUrl());
        }
    }

    @Override
    public void onDataSetChange(int start, int len, RealmOp op) {
        Log.v(TAG,"onDataSetChange(): op=" + op);
        if (op == RealmOp.OP_INSERT) {
            mAdapter.notifyItemRangeInserted(start, len );
            mRcvFavorList.smoothScrollToPosition(0);
        } else if (op == RealmOp.OP_DELETE) {
            mAdapter.notifyItemRangeRemoved(start, len);
        } else if (op == RealmOp.OP_MODIFY) {
            mAdapter.notifyItemRangeChanged(start, len);
        } else {
            mAdapter.notifyDataSetChanged();
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
    public void onContextMenuCreated(int pos, String adapter) {
        Log.v(TAG,"onContextMenuCreated()");

        mCurrentPos = pos;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.v(TAG,"onContextItemSelected()");

        switch (item.getItemId()) {
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

    private void initPresenter(){
        mPresenter = new FavorImagePresenterImpl(getContext(),this);
        mPresenter.init();
        setPresenter(mPresenter);
        mPresenter.onStart();
    }

}
