package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.github.rahatarmanahmed.cpv.CircularProgressViewListener;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.presenter.ImageType;
import com.github.runningforlife.photosniffer.presenter.NetState;
import com.github.runningforlife.photosniffer.presenter.Presenter;
import com.github.runningforlife.photosniffer.ui.UI;
import com.github.runningforlife.photosniffer.ui.activity.Refresh;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapterCallback;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.realm.RealmObject;
import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPickerActivity;

import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_FAVOR;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_GALLERY;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_WALLPAPER;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_DELETE;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_FAVOR;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_SAVE_AS_WALLPAPER;

/**
 * a abstract fragment class implemented by child
 */

public abstract class BaseFragment extends Fragment implements Refresh, UI, GalleryAdapterCallback {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LinearManager,GridManager, StaggeredManager})
    public @interface RecycleLayout{}
    public static final String LinearManager = "linearManager";
    public static final String GridManager = "GridManager";
    public static final String StaggeredManager = "StaggeredManager";

    static final String USER_SETTING_ADAPTER = "adapter";
    static final String ARGS_IMAGE_TYPE = "image_type";

    // current context menu item view position
    protected int mCurrentPos = -1;
    protected FragmentCallback mCallback;

    private Presenter mPresenter;
    private Menu mMenu;
    private MenuItem mVisibleMenu;
    private String mVisibleMenuTitle;
    GalleryAdapter mAdapter;

    ProgressBar mPbHint;
    private AlertDialog mHintAlert;

    // user setting adapter
    String mUserAdapter;
    String mUserAdapterPrefKey;
    int mImageType;

    public interface FragmentCallback {
        void onItemClick(View view, int pos, @ImageType int type);
        void onFragmentAttached();
        void showToast(String toast);
    }

    abstract boolean onOptionsMenuSelected(MenuItem menu);

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.v(TAG,"onPrepareOptionsMenu()");
        mMenu = menu;
        handleVisibleMenu();
        if (TextUtils.isEmpty(mVisibleMenuTitle)) {
            mVisibleMenuTitle = mVisibleMenu.getTitle().toString();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.add_wallpaper:
                startPhotoPicker();
                return true;
            case R.id.batch_edit:
                handleBatchEdit(true);
                return true;
            case R.id.favor:
                mPresenter.batchEdit(mAdapter.getSelectedImages(), BATCH_FAVOR);
                handleBatchEdit(false);
                return true;
            case R.id.delete:
                mPresenter.batchEdit(mAdapter.getSelectedImages(), BATCH_DELETE);
                handleBatchEdit(false);
                return true;
            case R.id.save_as_wallpaper:
                mPresenter.batchEdit(mAdapter.getSelectedImages(), BATCH_SAVE_AS_WALLPAPER);
                handleBatchEdit(false);
                return true;
            default:
                return onOptionsMenuSelected(menuItem);
        }
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        //option menu
        setHasOptionsMenu(true);
        // retain state; not recreate it again
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.d(TAG,"onStateInstanceState()");
    }

    @Override
    public void onViewStateRestored(Bundle state) {
        super.onViewStateRestored(state);
        Log.d(TAG,"onViewStateRestored()");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v(TAG,"onAttach()");
        try {
            mCallback = (FragmentCallback)context;
        }catch (ClassCastException e) {
            Log.e(TAG,"parent activity must implement FragmentCallback");
            throw new IllegalStateException("refresh callback must be implemented");
        }
    }

    @Override
    public void notifyJobState(boolean isStarted, String hint) {
        Log.v(TAG,"notifyJobState: is started=" + isStarted);
        if (isStarted) {
            if (mHintAlert == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                mHintAlert = builder.setView(R.layout.diaglog_busy_hint)
                        .create();

                Window window = mHintAlert.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(android.R.color.transparent);
                }
            }
            mHintAlert.show();
        } else {
            //mPbHint.setVisibility(View.GONE);
            if (mHintAlert.isShowing()) {
                mHintAlert.dismiss();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG,"onResume()");

        int resId = R.string.app_name;
        switch (mImageType) {
            case IMAGE_FAVOR:
                resId = R.string.favorite;
                break;
            case ImageType.IMAGE_GALLERY:
                resId = R.string.app_name;
                break;
            case ImageType.IMAGE_WALLPAPER:
                resId = R.string.set_wallpaper;
                break;
        }

        String title = getString(resId);
        setTitle(title);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG,"onActivityResult(): result code=" + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            List<String> photoUris = null;
            if (requestCode == PhotoPicker.REQUEST_CODE) {
                photoUris = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }

            if (photoUris != null && photoUris.size() > 0) {
                mPresenter.saveUserPickedPhotos(photoUris);
            }
        }

    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

    @Override
    public void setRefreshing(boolean enable) {
        // empty
    }

    protected void setPresenter(@NonNull Presenter presenter) {
        mPresenter = presenter;
    }

    abstract RecyclerView getRecycleView();

    protected void setTitle(String title) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(title);
        }
    }

    @Override
    public void onNetworkState(@NetState String state) {
        Log.v(TAG,"onNetworkState():state = " + state);
        if (mCallback != null && isAdded()) {
            switch (state) {
                case NetState.STATE_DISCONNECT:
                    mCallback.showToast(getString(R.string.network_not_connected));
                    break;
                case NetState.STATE_HUNG:
                    mCallback.showToast(getString(R.string.hint_network_state_hung));
                    break;
                case NetState.STATE_SLOW:
                    mCallback.showToast(getString(R.string.hint_network_state_slow));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone(): isOk = " + !TextUtils.isEmpty(path));
        if (isAdded()) {
            if (!TextUtils.isEmpty(path)) {
                mCallback.showToast(getString(R.string.save_image_Success) + path);
            } else {
                mCallback.showToast(getString(R.string.save_image_fail));
            }
        }
    }

    @Override
    public void onWallpaperSetDone(boolean isOk) {
        Log.v(TAG,"onWallpaperSetDone()");
        if (isAdded()) {
            if (isOk) {
                mCallback.showToast(getString(R.string.set_wallpaper_success));
            } else {
                mCallback.showToast(getString(R.string.set_wallpaper_fail));
            }
        }
    }

    @Override
    public void onItemSelected(int totalCount) {
        if (mVisibleMenu != null) {
            mVisibleMenu.setTitle(mVisibleMenuTitle + "( " + String.valueOf(totalCount) + ")");
        }
    }

    @Override
    public int getCount() {
        checkPresenter();
        return mPresenter.getItemCount();
    }

    @Override
    public RealmObject getItemAtPos(int pos) {
        checkPresenter();
        return mPresenter.getItemAtPos(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        checkPresenter();
        mPresenter.removeItemAtPos(pos);
    }

    @Override
    public void loadImageIntoView(int pos, ImageView iv, Priority priority, int w, int h, ImageView.ScaleType scaleType) {
        checkPresenter();
        mPresenter.loadImageIntoView(pos, iv, priority, w, h, scaleType);
    }

    void handleBatchEdit(boolean isEnabled) {
        Log.v(TAG,"handleBatchEdit()");
        if (isEnabled) {
            mMenu.setGroupVisible(R.id.group_usage, false);
            mMenu.setGroupVisible(R.id.group_batch_edit, true);
        } else {
            mMenu.setGroupVisible(R.id.group_usage, true);
            mMenu.setGroupVisible(R.id.group_batch_edit, false);
        }
        if (getRecycleView() != null) {
            getRecycleView().requestLayout();
        }

        handleVisibleMenu();

        if (mAdapter != null) {
            mAdapter.enableBatchEdit(isEnabled);
        }

        mVisibleMenu.setTitle(mVisibleMenuTitle);
    }

    private void handleVisibleMenu() {
        if (mImageType != IMAGE_GALLERY) {
            MenuItem menuItem = mMenu.findItem(R.id.favor);
            if (menuItem != null) {
                menuItem.setVisible(false);
            }
            if (mImageType == IMAGE_WALLPAPER) {
                MenuItem menuItem1 = mMenu.findItem(R.id.save_as_wallpaper);
                if (menuItem1 != null) {
                    menuItem1.setVisible(false);
                }

                MenuItem delete = mMenu.findItem(R.id.delete);
                delete.setShowAsAction(SHOW_AS_ACTION_IF_ROOM);

                mVisibleMenu = delete;
            } else {

                MenuItem save = mMenu.findItem(R.id.save_as_wallpaper);
                save.setShowAsAction(SHOW_AS_ACTION_IF_ROOM);

                mVisibleMenu = save;
            }
        } else {
            mVisibleMenu = mMenu.findItem(R.id.favor);
        }
    }

    private void startPhotoPicker() {
        Log.v(TAG,"startPhotoPicker");
        PhotoPicker.PhotoPickerBuilder builder = new PhotoPicker.PhotoPickerBuilder();
        builder.setGridColumnCount(3)
                .setPreviewEnabled(true)
                .setShowCamera(true)
                .setShowGif(false)
                .start(getContext(), this, PhotoPicker.REQUEST_CODE);
    }

    private void checkPresenter() {
        if (mPresenter == null) {
            throw new IllegalArgumentException("presenter should not be null");
        }
    }
}
