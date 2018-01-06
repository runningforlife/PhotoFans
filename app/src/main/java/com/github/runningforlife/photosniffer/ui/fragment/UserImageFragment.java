package com.github.runningforlife.photosniffer.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.presenter.ImageDetailPresenterImpl;
import com.github.runningforlife.photosniffer.presenter.RealmOp;
import com.github.runningforlife.photosniffer.ui.ImageDetailView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_WALLPAPER;

/**
 * fragment to manager wallpaper
 */

public class UserImageFragment extends BaseFragment implements ImageDetailView {
    public static final String TAG = "WallpaperFragment";

    @BindView(R.id.rcv_gallery) RecyclerView mRcvWallpaper;
    private ImageDetailPresenterImpl mPresenter;

    public static UserImageFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(ARGS_IMAGE_TYPE, type);
        UserImageFragment fragment = new UserImageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.v(TAG,"onCreate()");

        setRetainInstance(true);

        mUserAdapterPrefKey = TAG + "-" +  USER_SETTING_ADAPTER;
        mUserAdapter = SharedPrefUtil.getString(mUserAdapterPrefKey, GridManager);
        mAdapter = new GalleryAdapter(getActivity(), this);
        mAdapter.setLayoutManager(mUserAdapter);

        mImageType = getArguments().getInt(ARGS_IMAGE_TYPE);
        mPresenter = new ImageDetailPresenterImpl(Glide.with(this),
                getContext(), this, mImageType);
        setPresenter(mPresenter);
        mPresenter.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        Log.v(TAG,"onCreateView()");
        View root = inflater.inflate(R.layout.fragment_user_image, parent, false);

        ButterKnife.bind(this, root);

        initView();

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
        if (mCallback != null) {
            mCallback.onFragmentAttached();
        }
        //setTitle(getString(R.string.set_wallpaper));
    }

    @Override
    public void onItemClicked(View view, int pos, String adapter) {
        Log.v(TAG,"onItemClicked(): pos = " + pos);
        if (isAdded() && mCallback != null) {
            mCallback.onItemClick(view,pos, mImageType);
            // disable batch edit
            handleBatchEdit(false);
        }
    }

    @Override
    public RecyclerView getRecycleView() {
        return mRcvWallpaper;
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

    private boolean optionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String adapter = GridManager;
        switch (id) {
            case R.id.grid_view:
                GridLayoutManager glm = new GridLayoutManager(getContext(),2);
                mRcvWallpaper.setLayoutManager(glm);
                mRcvWallpaper.setHasFixedSize(true);
                glm.setAutoMeasureEnabled(true);
                break;
            case R.id.list_view:
                LinearLayoutManager ll = new LinearLayoutManager(getContext());
                mRcvWallpaper.setLayoutManager(ll);
                //ll.setAutoMeasureEnabled(true);
                adapter = LinearManager;
                break;
            case R.id.stagger_view:
                StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mRcvWallpaper.setLayoutManager(sglm);
                sglm.setAutoMeasureEnabled(true);
                adapter = StaggeredManager;
                break;
            default:
                break;
        }

        if (!mUserAdapter.equals(adapter)) {
            mAdapter.setLayoutManager(adapter);
            SharedPrefUtil.putString(mUserAdapterPrefKey, adapter);

            mRcvWallpaper.removeAllViews();
            mAdapter.notifyDataSetChanged();

            //Glide.with(this).onDestroy();
            mUserAdapter = adapter;
        }

        return true;
    }

    private void initView() {
        if (GridManager.equals(mUserAdapter)) {
            //LinearLayoutManager llMgr = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
            GridLayoutManager gridLayoutMgr = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
            gridLayoutMgr.setSmoothScrollbarEnabled(true);
            mRcvWallpaper.setHasFixedSize(true);
            mRcvWallpaper.setLayoutManager(gridLayoutMgr);
        } else if (LinearManager.equals(mUserAdapter)) {
            LinearLayoutManager ll = new LinearLayoutManager(getContext());
            mRcvWallpaper.setLayoutManager(ll);
            ll.setAutoMeasureEnabled(true);
            ll.setSmoothScrollbarEnabled(true);
        }

        mRcvWallpaper.setAdapter(mAdapter);
        mRcvWallpaper.setItemAnimator(new DefaultItemAnimator());
        mRcvWallpaper.setBackgroundResource(R.color.colorLightGrey);
    }

    @Override
    public void onImageLoadStart(int pos) {

    }

    @Override
    public void onImageLoadDone(int pos, boolean isSuccess) {

    }
}
