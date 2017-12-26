package com.github.runningforlife.photosniffer.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.presenter.ImageDetailPresenterImpl;
import com.github.runningforlife.photosniffer.presenter.ImageType;
import com.github.runningforlife.photosniffer.presenter.NetState;
import com.github.runningforlife.photosniffer.presenter.RealmOp;
import com.github.runningforlife.photosniffer.ui.ImageDetailView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmObject;

import com.github.runningforlife.photosniffer.ui.adapter.ImagePagerAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.PageAdapterCallback;
import com.github.runningforlife.photosniffer.ui.fragment.ActionListDialogFragment;
import com.github.runningforlife.photosniffer.data.model.UserAction;
import com.github.runningforlife.photosniffer.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.github.runningforlife.photosniffer.data.model.UserAction.*;

/**
 * activity to show the detail of the images
 *
 * FIXME:
 * java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position 0(offset:98).state:98
 */

public class ImageDetailActivity extends AppCompatActivity implements ImageDetailView,
        PageAdapterCallback,ActionListDialogFragment.ActionCallback {
    private static final String TAG = "ImageDetailActivity";

    @BindView(R.id.vp_image) ViewPager mImgPager;
    @BindView(R.id.cpv_load) CircularProgressView mCpvLoad;

    private ImagePagerAdapter mPagerAdapter;
    private ImageDetailPresenterImpl mPresenter;
    private int mCurrentImgIdx;
    private ActionBar mActionBar;
    private Handler mMainHandler;
    private ImagePageStateListener mPageListener;

    /**
     * user action to do operations allowed at the images
     */
    private List<String> mUserActionList;
    private static UserAction ACTION_SHARE = SHARE;
    private static UserAction ACTION_SAVE = SAVE;
    private static UserAction ACTION_WALLPAPER = WALLPAPER;
    private static UserAction ACTION_FAVOR = FAVOR;
    private static UserAction ACTION_DELETE = DELETE;

    private static final int PREVIEW_HIDE_COUNT_DOWN = 5000; // 5s

    @Override
    public void onCreate(Bundle savedSate){
        super.onCreate(savedSate);

        setContentView(R.layout.activity_image_detail);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();
        if(mActionBar != null){
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        initView();

        initPresenter();

        mMainHandler = new EventHandler(Looper.getMainLooper());

/*        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(null);
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG,"onResume()");
        //mPresenter.onStart();

        initActionList();

        setTitle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        //mMainHandler = null;
        mPresenter.onDestroy();
        mImgPager.removeOnPageChangeListener(mPageListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail,menu);

        return true;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        if(mCpvLoad.isActivated()){
            mCpvLoad.stopAnimation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){
            if(Build.VERSION.SDK_INT >= 21){
                finishAfterTransition();
            }else {
                finish();
            }
            return true;
        }else if(id == R.id.action_more) {
            showActionDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataSetChange(int start, int len, RealmOp op) {
        Log.v(TAG, "onDataSetChange(): op=" + op);
        if (mImgPager.getAdapter() == null) {
            mImgPager.setAdapter(mPagerAdapter);
        }

        mPagerAdapter.notifyDataSetChanged();
        // current index is changed
        mImgPager.setCurrentItem(mCurrentImgIdx);
        // update title
        setTitle();
    }

    @Override
    public void onWallpaperSetDone(boolean isOk) {
        Log.v(TAG,"onWallpaperSetDone()");
        String toastMsg;
        if (isOk) {
            toastMsg = getString(R.string.set_wallpaper_success);
        } else {
            toastMsg = getString(R.string.set_wallpaper_fail);
        }

        Message message = mMainHandler.obtainMessage(EventHandler.EVENT_SHOW_TOAST);
        message.obj = toastMsg;
        message.sendToTarget();
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
        Log.v(TAG,"onItemClicked(): pos " + pos);
        mCurrentImgIdx = pos;
        // loading image
        mImgPager.setCurrentItem(pos);
        // change title
        setTitle();
    }

    @Override
    public void onItemLongClicked(int pos, String adapter) {
        Log.v(TAG,"onItemLongClicked()");
        showActionDialog();
    }

    @Override
    public void onImageLoadStart(int pos) {
        Log.v(TAG,"onImageLoadStart()");
        mCpvLoad.setVisibility(View.VISIBLE);
        mCpvLoad.startAnimation();
    }

    @Override
    public void onImageLoadDone(int pos, boolean isSuccess) {
        Log.v(TAG,"onImageLoadDone()");
        mCpvLoad.stopAnimation();
        mCpvLoad.setVisibility(View.INVISIBLE);
    }

    @Override
    public void removeItemAtPos(final int pos) {
        mPresenter.removeItemAtPos(pos);

        mCurrentImgIdx = pos;

        final View obj = mImgPager.getChildAt(pos);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(obj, "alpha", 1f, 0f);
        alpha.setDuration(300);
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.v(TAG,"onAnimationEnd()");
                //mImgPager.removeViewAt(pos);
                mPagerAdapter.destroyItem(mImgPager, pos,obj);
                mPagerAdapter.notifyDataSetChanged();

                mImgPager.setCurrentItem(pos + 1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alpha.start();
        //mImgPager.removeView(obj);
    }

    @Override
    public void loadImageIntoView(int pos, ImageView iv, Priority priority, int w, int h, ImageView.ScaleType scaleType) {
        Log.v(TAG,"loadImageIntoView():pos = " + pos);
        mPresenter.loadImageIntoView(pos, iv, priority, w, h, scaleType);
    }

    @Override
    public void onActionClick(String action, int pos) {
        Log.v(TAG,"onActionClick(): action = " + action);

        mCurrentImgIdx = mImgPager.getCurrentItem();

        if (action.equals(ACTION_SAVE.action())) {
            // save image
            saveImage(mCurrentImgIdx);
        } else if(action.equals(ACTION_DELETE.action())) {
            // remove image
           removeItemAtPos(mCurrentImgIdx);
            // refresh data at once
            //mPresenter.onStart();
        } else if(action.equals(ACTION_FAVOR.action())) {
            // favor this image FIXME: delay action to activity exit
            mPresenter.favorImageAtPos(mCurrentImgIdx);
        } else if (action.equals(ACTION_SHARE.action())) {

        } else if (action.equals(ACTION_WALLPAPER.action())) {
            setWallpaper(mCurrentImgIdx);
        }

        // hide fragment
        FragmentManager ft = getSupportFragmentManager();

        ft.beginTransaction()
          .detach(ft.findFragmentByTag("ActionList"))
          .commit();
    }

    private void initView() {
        Log.v(TAG,"initView()");
        mPagerAdapter = new ImagePagerAdapter(this ,this);

        mPageListener = new ImagePageStateListener();
        mImgPager.addOnPageChangeListener(mPageListener);
        mImgPager.setOffscreenPageLimit(3);

        Intent data = getIntent();
        mCurrentImgIdx = data.getIntExtra("image",0);
        mImgPager.setCurrentItem(mCurrentImgIdx);
    }

    private void initPresenter() {
        mPresenter = new ImageDetailPresenterImpl(Glide.with(this), getApplicationContext(),ImageDetailActivity.this, ImageType.IMAGE_GALLERY);
        // may have null pointer exception
        mPresenter.onStart();

        mImgPager.setAdapter(mPagerAdapter);
    }

    private void initActionList() {
        mUserActionList = new ArrayList<>();
        String share = getString(R.string.action_share);
        String save = getString(R.string.action_save);
        String wallpaper = getString(R.string.action_wallpaper);
        String delete = getString(R.string.action_delete);
        String favor = getString(R.string.action_favorite);

        //mUserActionList.add(share);
        mUserActionList.add(save);
        mUserActionList.add(favor);
        mUserActionList.add(wallpaper);
        mUserActionList.add(delete);

        ACTION_SHARE.setAction(share);
        ACTION_DELETE.setAction(delete);
        ACTION_FAVOR.setAction(favor);
        ACTION_SAVE.setAction(save);
        ACTION_WALLPAPER.setAction(wallpaper);
    }

    @SuppressLint("RestrictedApi")
    private void setTitle() {
        String title = (mCurrentImgIdx + 1) + "/" + mPresenter.getItemCount();
        mActionBar.setWindowTitle(title);
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone(): isOk = " + !TextUtils.isEmpty(path));
        String toastString;
        if(!TextUtils.isEmpty(path)) {
            toastString = getString(R.string.save_image_Success) + path;
        }else{
            toastString = getString(R.string.save_image_fail);
        }
        Message msg = mMainHandler.obtainMessage(EventHandler.EVENT_SHOW_TOAST);
        msg.obj = toastString;
        msg.sendToTarget();
    }

    @Override
    public void onNetworkState(@NetState String state) {
        Log.v(TAG,"onNetworkState(): state=" + state);
        String toastString = null;
        switch (state) {
            case NetState.STATE_DISCONNECT:
                toastString = getString(R.string.network_not_connected);
                break;
            case NetState.STATE_HUNG:
                toastString = getString(R.string.hint_network_state_hung);
                break;
            case NetState.STATE_SLOW:
                toastString = getString(R.string.hint_network_state_slow);
                break;
            default:
                break;
        }

        if (!TextUtils.isEmpty(toastString)) {
            Message message = mMainHandler.obtainMessage(EventHandler.EVENT_SHOW_TOAST);
            message.obj = toastString;
            message.sendToTarget();
        }

    }

    private void showToast(String msg) {
        Log.v(TAG,"showToast()");
        ToastUtil.showToast(this, msg);
    }

    private final class ImagePageStateListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // do nothing
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onPageSelected(int position) {
            Log.v(TAG,"onPageSelected()");
            mCurrentImgIdx = position;
            setTitle();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                mPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showActionDialog() {
        ActionListDialogFragment fragment = (ActionListDialogFragment)
                ActionListDialogFragment.newInstance(mUserActionList);
        fragment.setCallback(this);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
          .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
          //.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_to_bottom)
          .add(fragment,"ActionList")
          .commit();
    }

    private void saveImage(int pos){
        Log.v(TAG,"saveImage()");
        mPresenter.saveImageAtPos(pos);
    }

    private void setWallpaper(int pos){
        Log.v(TAG,"setWallpaperAtPos()");
        mPresenter.setWallpaperAtPos(pos);
    }


    private class EventHandler extends Handler {

        static final int IMAGE_SAVE_COMPLETE = 1;
        static final int IMAGE_SAVE_FAIL = 2;
        static final int IMAGE_SHARE_COMPLETE = 5;
        static final int EVENT_HIDE_PREVIEW = 6;
        static final int EVENT_SHOW_TOAST = 10;

        EventHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;

            switch (what){
                case IMAGE_SAVE_COMPLETE:
                    String toast = getString(R.string.save_image_Success) + msg.obj;
                    ToastUtil.showToast(ImageDetailActivity.this,toast);
                    break;
                case IMAGE_SAVE_FAIL:
                    String toast1 = getString(R.string.save_image_fail);
                    ToastUtil.showToast(ImageDetailActivity.this,toast1);
                case EVENT_HIDE_PREVIEW:
                    break;
                case EVENT_SHOW_TOAST:
                    showToast((String)msg.obj);
                    break;
                default:
                    break;
            }
        }
    }
}
