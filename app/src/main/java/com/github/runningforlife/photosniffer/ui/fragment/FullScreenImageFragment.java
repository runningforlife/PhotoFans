package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.data.local.RealmManager;
import com.github.runningforlife.photosniffer.data.model.UserAction;
import com.github.runningforlife.photosniffer.presenter.ImageSaveRunnable;
import com.github.runningforlife.photosniffer.utils.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.runningforlife.photosniffer.data.model.UserAction.DELETE;
import static com.github.runningforlife.photosniffer.data.model.UserAction.FAVOR;
import static com.github.runningforlife.photosniffer.data.model.UserAction.SAVE;
import static com.github.runningforlife.photosniffer.data.model.UserAction.WALLPAPER;

/**
 * a dialog to show full screen image
 */

public class FullScreenImageFragment extends BaseFragment implements ActionListDialogFragment.ActionCallback{
    public static final String TAG = "FullScreenImageFragment";
    public static final String POSITION = "position";
    public static final String IMAGE_URL = "image_url";

    @BindView(R.id.iv_image) ImageView imageView;

    private List<String> mUserActionList;
    private static UserAction ACTION_SAVE = SAVE;
    private static UserAction ACTION_FAVOR = FAVOR;
    private static UserAction ACTION_WALLPAPER = WALLPAPER;
    private static UserAction ACTION_DELETE = DELETE;

    public static FullScreenImageFragment newInstance(String url){
        FullScreenImageFragment fragment = new FullScreenImageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(IMAGE_URL, url);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedSate){
        super.onCreate(savedSate);

        initDialogWindow();

        initActionList();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG, "onResume()");
        //setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        Log.v(TAG,"onCreateView()");
        View root = inflater.inflate(R.layout.fragment_full_screen,parent,false);

        ButterKnife.bind(this, root);

        initView();

        return root;
    }

    private void initDialogWindow(){
        Log.v(TAG,"initDialogWindow()");
        Window window = getActivity().getWindow();
        if(window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.gravity = Gravity.CENTER;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }


    private void initView(){
        Log.v(TAG,"initView()");
        if(Build.VERSION.SDK_INT >= 21) {
            // transition name
            String pos = getArguments().getString(POSITION);
            String transitionName = getString(R.string.activity_image_transition)
                    + pos;
            imageView.setTransitionName(transitionName);
        }

        String url = getArguments().getString(IMAGE_URL);
        if(!TextUtils.isEmpty(url)) {
            GlideLoaderListener listener = new GlideLoaderListener(imageView);
            listener.setScaleType(ImageView.ScaleType.FIT_CENTER);
            GlideLoader.downloadOnly(getContext(), url, listener, Priority.IMMEDIATE,
                    Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, false);
        }

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showActionListFragment();
                return true;
            }
        });
    }

    private void showActionListFragment(){
        if(getActivity() != null) {
            FragmentManager fragmentMgr = getActivity().getSupportFragmentManager();

            ActionListDialogFragment fragment = (ActionListDialogFragment) fragmentMgr.findFragmentByTag(ActionListDialogFragment.TAG);
            if (fragment == null) {
                fragment = (ActionListDialogFragment) ActionListDialogFragment.newInstance(mUserActionList);
            }
            fragment.setCallback(this);

            fragmentMgr.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    //.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_to_bottom)
                    .add(fragment, ActionListDialogFragment.TAG)
                    .commit();
        }
    }

    private void initActionList(){
        mUserActionList = new ArrayList<>();
        //String share = getString(R.string.action_share);
        String save = getString(R.string.action_save);
        String wallpaper = getString(R.string.action_wallpaper);
        String delete = getString(R.string.action_delete);
        String favor = getString(R.string.action_favorite);

        //mUserActionList.add(share);
        mUserActionList.add(save);
        mUserActionList.add(wallpaper);
        //mUserActionList.add(delete);
        //mUserActionList.add(favor);

        ACTION_DELETE.setAction(delete);
        ACTION_FAVOR.setAction(favor);
        ACTION_SAVE.setAction(save);
        ACTION_WALLPAPER.setAction(wallpaper);
    }

    @Override
    public void onActionClick(String action, int pos) {
        Log.v(TAG,"onActionClick(): action = " + action);

        if(action.equals(ACTION_SAVE.action())){
            // save image
            saveImage();
        }else if(action.equals(ACTION_WALLPAPER.action())){
            setWallpaper();
        }
    }

    private void saveImage(){
        String url = getArguments().getString(IMAGE_URL);
        if(!TextUtils.isEmpty(url)){
            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG,"onImageLoadDone()");
                    if(o instanceof Bitmap) {
                        final ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o),
                                String.valueOf(System.currentTimeMillis()));
                        r.addCallback(new ImageSaveRunnable.ImageSaveCallback() {
                            @Override
                            public void onImageSaveDone(String path) {
                                Log.v(TAG,"onImageSaveDone()");
                                String msg = getString(R.string.save_image_Success) + path;
                                Message message = mHandler.obtainMessage(EVENT_SHOW_TOAST);
                                message.obj = msg;
                                message.sendToTarget();
                            }
                        });
                        // starting save image
                        new Thread(r).start();
                    }
                }
            });
            GlideLoader.downloadOnly(getContext(), url, listener,
                    Priority.HIGH,Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, false);
        }
    }

    private void setWallpaper(){
        Log.v(TAG,"setWallpaper()");
        final String url = getArguments().getString(IMAGE_URL);
        if(!TextUtils.isEmpty(url)){
            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG,"onImageLoadDone()");
                    if(o instanceof Bitmap) {
                        WallpaperManager wpm = WallpaperManager.getInstance(getContext());
                        try {
                            wpm.setBitmap((Bitmap)o);
                            // mark it as wall paper
                            RealmManager.getInstance().setWallpaper(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            GlideLoader.downloadOnly(getContext(), url, listener,
                    Priority.HIGH,Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, true);
        }
    }

    private static final int EVENT_SHOW_TOAST = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            switch (what){
                case EVENT_SHOW_TOAST:
                    String toast = (String) msg.obj;
                    ToastUtil.showToast(getActivity(), toast);
                    break;
                default:
                    break;
            }
        }
    };
}
