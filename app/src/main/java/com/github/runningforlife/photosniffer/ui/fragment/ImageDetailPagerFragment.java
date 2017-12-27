package com.github.runningforlife.photosniffer.ui.fragment;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.presenter.ImageDetailPresenterImpl;
import com.github.runningforlife.photosniffer.presenter.ImageType;
import com.github.runningforlife.photosniffer.presenter.RealmOp;
import com.github.runningforlife.photosniffer.ui.ImageDetailView;
import com.github.runningforlife.photosniffer.ui.activity.UserAction;
import com.github.runningforlife.photosniffer.ui.adapter.ImagePagerAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.PageAdapterCallback;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmObject;

import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_FAVOR;
import static com.github.runningforlife.photosniffer.ui.activity.UserAction.DELETE;
import static com.github.runningforlife.photosniffer.ui.activity.UserAction.FAVOR;
import static com.github.runningforlife.photosniffer.ui.activity.UserAction.SAVE;
import static com.github.runningforlife.photosniffer.ui.activity.UserAction.SHARE;
import static com.github.runningforlife.photosniffer.ui.activity.UserAction.WALLPAPER;

/**
 * a fragment containing view pager,
 * refer to me.iwf.photopicker.fragment.ImagePagerFragment
 */

public class ImageDetailPagerFragment extends Fragment
        implements PageAdapterCallback, ImageDetailView, ActionListDialogFragment.ActionCallback{
    public static final String TAG = "ImagePagerFragment";

    public final static String ARG_PATH = "PATHS";
    public final static String ARG_CURRENT_ITEM = "ARG_CURRENT_ITEM";

    private ArrayList<String> mImageUrls;
    private ViewPager mViewPager;
    private ImagePagerAdapter mPagerAdapter;

    public final static long ANIM_DURATION = 200L;

    public final static String ARG_THUMBNAIL_TOP    = "THUMBNAIL_TOP";
    public final static String ARG_THUMBNAIL_LEFT   = "THUMBNAIL_LEFT";
    public final static String ARG_THUMBNAIL_WIDTH  = "THUMBNAIL_WIDTH";
    public final static String ARG_THUMBNAIL_HEIGHT = "THUMBNAIL_HEIGHT";
    public final static String ARG_HAS_ANIM = "HAS_ANIM";
    public final static String ARG_IMAGE_TYPE = "IMAGE_TYPE";

    private int thumbnailTop    = 0;
    private int thumbnailLeft   = 0;
    private int thumbnailWidth  = 0;
    private int thumbnailHeight = 0;

    private boolean hasAnim = false;

    private final ColorMatrix colorizerMatrix = new ColorMatrix();

    private int currentItem = 0;
    // presenter
    private ImageDetailPresenterImpl mPresenter;
    private @ImageType int mImageType;
    // to restore title
    private CharSequence mOriginalTitle;

    private List<String> mUserActions;
    private static UserAction ACTION_SHARE = SHARE;
    private static UserAction ACTION_SAVE = SAVE;
    private static UserAction ACTION_WALLPAPER = WALLPAPER;
    private static UserAction ACTION_FAVOR = FAVOR;
    private static UserAction ACTION_DELETE = DELETE;


    public static ImageDetailPagerFragment newInstance(int pos, int[] screenLocation, int thumbnailWidth, int thumbnailHeight, @ImageType int type) {
        ImageDetailPagerFragment f = newInstance(pos);

        Bundle args = f.getArguments();

        args .putInt(ARG_THUMBNAIL_LEFT, screenLocation[0]);
        args .putInt(ARG_THUMBNAIL_TOP, screenLocation[1]);
        args .putInt(ARG_THUMBNAIL_WIDTH, thumbnailWidth);
        args .putInt(ARG_THUMBNAIL_HEIGHT, thumbnailHeight);
        args .putBoolean(ARG_HAS_ANIM, true);
        args.putInt(ARG_IMAGE_TYPE, type);

        return f;
    }

    public static ImageDetailPagerFragment newInstance(int currentItem) {

        ImageDetailPagerFragment f = new ImageDetailPagerFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_ITEM, currentItem);
        args.putBoolean(ARG_HAS_ANIM, false);

        f.setArguments(args);

        return f;
    }

    public void setPhotos(List<String> paths, int currentItem) {
        this.mImageUrls.clear();
        this.mImageUrls.addAll(paths);
        this.currentItem = currentItem;

        mViewPager.setCurrentItem(currentItem);
        mViewPager.getAdapter().notifyDataSetChanged();
    }


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageUrls = new ArrayList<>();

        Bundle bundle = getArguments();

        if (bundle != null) {
            String[] pathArr = bundle.getStringArray(ARG_PATH);
            mImageUrls.clear();
            if (pathArr != null) {

                mImageUrls = new ArrayList<>(Arrays.asList(pathArr));
            }

            hasAnim         = bundle.getBoolean(ARG_HAS_ANIM);
            currentItem     = bundle.getInt(ARG_CURRENT_ITEM);
            thumbnailTop    = bundle.getInt(ARG_THUMBNAIL_TOP);
            thumbnailLeft   = bundle.getInt(ARG_THUMBNAIL_LEFT);
            thumbnailWidth  = bundle.getInt(ARG_THUMBNAIL_WIDTH);
            thumbnailHeight = bundle.getInt(ARG_THUMBNAIL_HEIGHT);
            mImageType      = bundle.getInt(ARG_IMAGE_TYPE);
        }

        mPagerAdapter = new ImagePagerAdapter(getActivity(), this);

        setHasOptionsMenu(true);

        initActionList();
    }


    @Nullable
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {

        View rootView = inflater.inflate(me.iwf.photopicker.R.layout.__picker_picker_fragment_image_pager, container, false);

        mViewPager = rootView.findViewById(me.iwf.photopicker.R.id.vp_photos);
        mViewPager.setOffscreenPageLimit(5);

        initPresenter();
        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null && hasAnim) {
            ViewTreeObserver observer = mViewPager.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    mViewPager.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    mViewPager.getLocationOnScreen(screenLocation);
                    thumbnailLeft = thumbnailLeft - screenLocation[0];
                    thumbnailTop  = thumbnailTop - screenLocation[1];

                    runEnterAnimation();

                    return true;
                }
            });
        }


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override public void onPageSelected(int position) {
                hasAnim = currentItem == position;

                setTitle(position);
            }

            @Override public void onPageScrollStateChanged(int state) {

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity parent = (AppCompatActivity) getActivity();
        ActionBar toolbar = parent.getSupportActionBar();
        if (toolbar != null) {
            mOriginalTitle = toolbar.getTitle();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem mi = menu.findItem(R.id.add_wallpaper);
        if (mi != null) {
            mi.setVisible(false);
        }
        mi = menu.findItem(R.id.grid_view);
        if (mi != null) {
            mi.setVisible(false);
        }
        mi = menu.findItem(R.id.list_view);
        if (mi != null) {
            mi.setVisible(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Log.v(TAG,"onOptionsItemSelected()");
        switch (menuItem.getItemId()) {
            case R.id.action_more:
                showActionDialog();
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    private void initPresenter() {
        mPresenter = new ImageDetailPresenterImpl(Glide.with(this), getActivity(), this, mImageType);
        mPresenter.onStart();
    }

    private void initActionList() {
        mUserActions = new ArrayList<>();
        String save = getString(R.string.action_save);
        String wallpaper = getString(R.string.action_wallpaper);
        String delete = getString(R.string.action_delete);
        String favor = getString(R.string.action_favorite);

        mUserActions.add(wallpaper);
        if (mImageType != IMAGE_FAVOR) {
            mUserActions.add(favor);
        }
        mUserActions.add(save);
        mUserActions.add(delete);

        ACTION_DELETE.setAction(delete);
        ACTION_FAVOR.setAction(favor);
        ACTION_SAVE.setAction(save);
        ACTION_WALLPAPER.setAction(wallpaper);
    }

    private void showActionDialog() {
        ActionListDialogFragment fragment = (ActionListDialogFragment)
                ActionListDialogFragment.newInstance(mUserActions);
        fragment.setCallback(this);

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction()
                //.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_to_bottom)
                .add(fragment,"ActionList")
                .addToBackStack(null)
                .commit();
    }


    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    private void runEnterAnimation() {
        final long duration = ANIM_DURATION;

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        ViewHelper.setPivotX(mViewPager, 0);
        ViewHelper.setPivotY(mViewPager, 0);
        ViewHelper.setScaleX(mViewPager, (float) thumbnailWidth / mViewPager.getWidth());
        ViewHelper.setScaleY(mViewPager, (float) thumbnailHeight / mViewPager.getHeight());
        ViewHelper.setTranslationX(mViewPager, thumbnailLeft);
        ViewHelper.setTranslationY(mViewPager, thumbnailTop);

        // Animate scale and translation to go from thumbnail to full size
        ViewPropertyAnimator.animate(mViewPager)
                .setDuration(duration)
                .scaleX(1)
                .scaleY(1)
                .translationX(0)
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator());

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // Animate a color filter to take the image from grayscale to full color.
        // This happens in parallel with the image scaling and moving into place.
        ObjectAnimator colorizer = ObjectAnimator.ofFloat(this,
                "saturation", 0, 1);
        colorizer.setDuration(duration);
        colorizer.start();

    }


    /**
     * The exit animation is basically a reverse of the enter animation, except that if
     * the orientation has changed we simply scale the picture back into the center of
     * the screen.
     *
     * @param endAction This action gets run after the animation completes (this is
     * when we actually switch activities)
     */
    public void runExitAnimation(final Runnable endAction) {
        // restore parent title
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mOriginalTitle);
        }

        if (!getArguments().getBoolean(ARG_HAS_ANIM, false) || !hasAnim) {
            endAction.run();
            return;
        }

        final long duration = ANIM_DURATION;

        // Animate image back to thumbnail size/location
        ViewPropertyAnimator.animate(mViewPager)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .scaleX((float) thumbnailWidth / mViewPager.getWidth())
                .scaleY((float) thumbnailHeight / mViewPager.getHeight())
                .translationX(thumbnailLeft)
                .translationY(thumbnailTop)
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animation) {
                    }
                    @Override public void onAnimationEnd(Animator animation) {
                        endAction.run();
                    }
                    @Override public void onAnimationCancel(Animator animation) {
                    }
                    @Override public void onAnimationRepeat(Animator animation) {
                    }
                });

        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // Animate a color filter to take the image back to grayscale,
        // in parallel with the image scaling and moving into place.
        ObjectAnimator colorizer =
                ObjectAnimator.ofFloat(this, "saturation", 1, 0);
        colorizer.setDuration(duration);
        colorizer.start();
    }


    /**
     * This is called by the colorizing animator. It sets a saturation factor that is then
     * passed onto a filter on the picture's drawable.
     * @param value saturation
     */
    public void setSaturation(float value) {
        colorizerMatrix.setSaturation(value);
        ColorMatrixColorFilter colorizerFilter = new ColorMatrixColorFilter(colorizerMatrix);
        mViewPager.getBackground().setColorFilter(colorizerFilter);
    }

    @Override public void onDestroy() {
        super.onDestroy();

        mImageUrls.clear();
        mImageUrls = null;

        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
    }

    @Override
    public void onActionClick(String action, int pos) {
        if (ACTION_WALLPAPER.action().equals(action)) {
            mPresenter.setWallpaperAtPos(pos);
        } else if (ACTION_FAVOR.action().equals(action)) {
            mPresenter.favorImageAtPos(pos);
        } else if (ACTION_SAVE.action().equals(action)) {
            mPresenter.saveImageAtPos(pos);
        } else if (ACTION_DELETE.action().equals(action)) {
            mPresenter.removeItemAtPos(pos);
        }

        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
    }

    @Override
    public void onItemLongClicked(int pos, String adapter) {
        showActionDialog();
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
        Log.v(TAG,"onItemClicked()");
        (getActivity()).onBackPressed();
    }

    @Override
    public void removeItemAtPos(int pos) {
        mPresenter.removeItemAtPos(pos);
    }

    @Override
    public void loadImageIntoView(int pos, ImageView iv, Priority priority, int w, int h, ImageView.ScaleType scaleType) {
        mPresenter.loadImageIntoView(pos, iv, priority, w, h, scaleType);
    }

    private void setTitle(int current) {
        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (toolbar != null) {
            String title = (current + 1) + "/" + mPresenter.getItemCount();
            toolbar.setTitle(title);
        }
    }

    @Override
    public void onImageLoadStart(int pos) {

    }

    @Override
    public void onImageLoadDone(int pos, boolean isSuccess) {

    }

    @Override
    public void onDataSetChange(int start, int end, RealmOp op) {
        if (mViewPager.getAdapter() == null) {
            mViewPager.setAdapter(mPagerAdapter);
        }
        mPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(currentItem);

        setTitle(currentItem);
    }

    @Override
    public void onImageSaveDone(String path) {

    }

    @Override
    public void onWallpaperSetDone(boolean isOk) {

    }

    @Override
    public void onNetworkState(String state) {

    }
}
