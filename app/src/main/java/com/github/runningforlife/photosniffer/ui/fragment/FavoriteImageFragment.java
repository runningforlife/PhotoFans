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

import com.bumptech.glide.Glide;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.presenter.FavorImagePresenterImpl;
import com.github.runningforlife.photosniffer.presenter.ImageType;
import com.github.runningforlife.photosniffer.presenter.RealmOp;
import com.github.runningforlife.photosniffer.ui.FavorPictureView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmObject;

import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_FAVOR;

/**
 * a fragment containing all favorite images
 */

public class FavoriteImageFragment extends BaseFragment implements FavorPictureView {
    public static final String TAG = "FavorImageFragment";
    @BindView(R.id.rcv_gallery) RecyclerView mRcvFavorList;
    GalleryAdapter mAdapter;
    private FavorImagePresenterImpl mPresenter;
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
    boolean onOptionsMenuSelected(MenuItem menu) {
        Log.v(TAG,"onOptionsMenuSelected()");
        return optionsItemSelected(menu);
    }

    @Override
    public void onResume(){
        super.onResume();

        if(mCallback != null) {
            mCallback.onFragmentAttached();
        }

        setTitle(getString(R.string.my_favorite_images));
    }

    private void initView() {
        mUserAdapterPrefKey = TAG + "-" +  USER_SETTING_ADAPTER;
        mUserAdapter = SharedPrefUtil.getString(mUserAdapterPrefKey, GridManager);

        if (GridManager.equals(mUserAdapter)) {
            //LinearLayoutManager llMgr = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
            GridLayoutManager gridLayoutMgr = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
            gridLayoutMgr.setSmoothScrollbarEnabled(true);
            mRcvFavorList.setHasFixedSize(true);
            mRcvFavorList.setLayoutManager(gridLayoutMgr);
        } else if (LinearManager.equals(mUserAdapter)) {
            LinearLayoutManager ll = new LinearLayoutManager(getContext());
            mRcvFavorList.setLayoutManager(ll);
            ll.setAutoMeasureEnabled(true);
            ll.setSmoothScrollbarEnabled(true);
        }
        mRcvFavorList.setItemAnimator(new DefaultItemAnimator());
        // restore back ground
        mRcvFavorList.setBackgroundResource(R.color.colorLightGrey);

        mAdapter = new GalleryAdapter(getActivity(),this);
        mAdapter.setLayoutManager(mUserAdapter);

        mRcvFavorList.setAdapter(mAdapter);
    }

    @Override
    public void onItemClicked(View view, int pos, String adapter) {
        Log.v(TAG,"onItemClicked(): pos = " + pos);

        if(isAdded() && mCallback != null){
            mCallback.onItemClick(view,pos, IMAGE_FAVOR);
        }
    }

    @Override
    public void onDataSetChange(int start, int len, RealmOp op) {
        Log.v(TAG,"onDataSetChange(): op=" + op);
        if (op == RealmOp.OP_INSERT) {
            mAdapter.notifyItemRangeInserted(start, len);
            mRcvFavorList.smoothScrollToPosition(0);
        } else if (op == RealmOp.OP_DELETE) {
            mAdapter.notifyItemRangeRemoved(start, len);
        } else if (op == RealmOp.OP_MODIFY) {
            mAdapter.notifyItemRangeChanged(start, len);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean optionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.grid_view:
                GridLayoutManager glm = new GridLayoutManager(getContext(),2);
                mRcvFavorList.setLayoutManager(glm);
                mRcvFavorList.setHasFixedSize(true);
                glm.setAutoMeasureEnabled(true);
                mUserAdapter = GridManager;
                break;
            case R.id.list_view:
                LinearLayoutManager ll = new LinearLayoutManager(getContext());
                mRcvFavorList.setLayoutManager(ll);
                ll.setAutoMeasureEnabled(true);
                mUserAdapter = LinearManager;
                break;
            case R.id.stagger_view:
                StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mRcvFavorList.setLayoutManager(sglm);
                sglm.setAutoMeasureEnabled(true);
                mUserAdapter = StaggeredManager;
                break;
            default:
                return false;

        }

        mAdapter.setLayoutManager(mUserAdapter);
        SharedPrefUtil.putString(mUserAdapterPrefKey, mUserAdapter);

        mRcvFavorList.removeAllViews();
        mAdapter.notifyDataSetChanged();

        return true;
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

    private void initPresenter() {
        mPresenter = new FavorImagePresenterImpl(Glide.with(this),getContext(),this);
        setPresenter(mPresenter);
        mPresenter.onStart();
    }

}
