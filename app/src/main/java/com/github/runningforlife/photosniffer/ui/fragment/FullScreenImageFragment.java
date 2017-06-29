package com.github.runningforlife.photosniffer.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.model.UserAction;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.runningforlife.photosniffer.model.UserAction.DELETE;
import static com.github.runningforlife.photosniffer.model.UserAction.FAVOR;
import static com.github.runningforlife.photosniffer.model.UserAction.SAVE;
import static com.github.runningforlife.photosniffer.model.UserAction.WALLPAPER;

/**
 * a dialog to show full screen image
 */

public class FullScreenImageFragment extends BaseFragment implements ActionListDialogFragment.ActionCallback{
    public static final String TAG = "FullScreenImageFragment";
    private static final String IMAGE_URL = "image_url";

    @BindView(R.id.toolbar) Toolbar toolbar;
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                FragmentManager fragmentMgr = getActivity().getSupportFragmentManager();
                fragmentMgr.popBackStackImmediate();
                break;
        }

        return false;
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
        toolbar.setNavigationIcon(R.drawable.ic_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentMgr = getActivity().getSupportFragmentManager();
                fragmentMgr.popBackStackImmediate();
            }
        });

        String url = getArguments().getString(IMAGE_URL);
        if(!TextUtils.isEmpty(url)) {
            GlideLoaderListener listener = new GlideLoaderListener(imageView);
            listener.setScaleType(ImageView.ScaleType.FIT_CENTER);
            GlideLoader.downloadOnly(getContext(), url, listener,
                    Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
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
        FragmentManager fragmentMgr = getChildFragmentManager();

        ActionListDialogFragment fragment = (ActionListDialogFragment) ActionListDialogFragment.newInstance(mUserActionList);

        fragment.show(fragmentMgr, ActionListDialogFragment.TAG);
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
        mUserActionList.add(delete);
        mUserActionList.add(favor);

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

        }else if(action.equals(ACTION_DELETE.action())){
            // remove image

        }else if(action.equals(ACTION_FAVOR.action())){
            // favor this image
        }else if(action.equals(ACTION_WALLPAPER.action())){

        }
    }
}
