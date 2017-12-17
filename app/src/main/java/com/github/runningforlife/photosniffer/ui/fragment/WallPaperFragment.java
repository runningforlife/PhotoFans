package com.github.runningforlife.photosniffer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
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
import com.github.runningforlife.photosniffer.data.model.ImageRealm;

import com.github.runningforlife.photosniffer.presenter.RealmOp;
import com.github.runningforlife.photosniffer.presenter.WallpaperPresenterImpl;
import com.github.runningforlife.photosniffer.ui.WallpaperView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapterCallback;
import com.github.runningforlife.photosniffer.ui.anim.ScaleInOutItemAnimator;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmObject;

/**
 * fragment to manager wallpaper
 */

public class WallPaperFragment extends BaseFragment implements WallpaperView {
    public static final String TAG = "WallpaperFragment";

    @BindView(R.id.rcv_gallery) RecyclerView mRcvWallpaper;

    private GalleryAdapter mAdapter;
    private WallpaperPresenterImpl mPresenter;

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
    boolean onOptionsMenuSelected(MenuItem menu) {
        Log.v(TAG,"onOptionsMenuSelected()");
        return optionsItemSelected(menu);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mCallback != null){
            mCallback.onFragmentAttached();
        }

        setTitle(getString(R.string.set_wallpaper));
    }

    @Override
    public void onItemClicked(View view, int pos, String adapter) {
        Log.v(TAG,"onItemClicked(): pos = " + pos);
        if(mCallback != null){
            mCallback.onItemClick(view,pos, mPresenter.getItemAtPos(pos).getUrl());
        }
    }

    @Override
    public void onDataSetChange(int start, int len, RealmOp op) {
        Log.v(TAG,"onDataSetChange(): op=" + op);
        if (op == RealmOp.OP_INSERT) {
            mAdapter.notifyItemRangeInserted(start,  len);
            mRcvWallpaper.smoothScrollToPosition(0);
        } else if (op == RealmOp.OP_DELETE) {
            mAdapter.notifyItemRangeRemoved(start, len);
        } else if (op == RealmOp.OP_MODIFY) {
            mAdapter.notifyItemRangeChanged(start, len);
        } else {
            mAdapter.notifyDataSetChanged();
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

    private boolean optionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.grid_view:
                GridLayoutManager glm = new GridLayoutManager(getContext(),2);
                mRcvWallpaper.setLayoutManager(glm);
                glm.setAutoMeasureEnabled(true);
                return true;
            case R.id.list_view:
                LinearLayoutManager ll = new LinearLayoutManager(getContext());
                mRcvWallpaper.setLayoutManager(ll);
                ll.setAutoMeasureEnabled(true);
                return true;
            case R.id.stagger_view:
                StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mRcvWallpaper.setLayoutManager(sglm);
                sglm.setAutoMeasureEnabled(true);
                return true;
        }

        mRcvWallpaper.removeAllViews();
        mAdapter.notifyDataSetChanged();

        return false;
    }

    private void initView() {
        StaggeredGridLayoutManager sgm = new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL);
        mRcvWallpaper.setLayoutManager(sgm);

        mAdapter = new GalleryAdapter(getActivity(), this);
        mRcvWallpaper.setAdapter(mAdapter);
        mRcvWallpaper.setItemAnimator(new DefaultItemAnimator());
        //mRcvWallpaper.setItemAnimator(new ScaleInOutItemAnimator());
        mRcvWallpaper.setBackgroundResource(R.color.colorLightGrey);
    }

    private void initPresenter() {
        mPresenter = new WallpaperPresenterImpl(getContext(), this);
        setPresenter(mPresenter);
        mPresenter.onStart();
    }
}
