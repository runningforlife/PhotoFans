package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;

/**
 * a dialog to show full screen image
 */

public class FullScreenImageFragment extends DialogFragment{
    public static final String TAG = "FullScreenImageFragment";
    private static final String IMAGE_URL = "image_url";

    private ItemLongClickedListener mListener;

    public interface ItemLongClickedListener{
        void onImageLongClicked(String url);
    }

    public static FullScreenImageFragment newInstance(String url){
        FullScreenImageFragment fragment = new FullScreenImageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(IMAGE_URL, url);
        fragment.setArguments(bundle);

        return fragment;
    }

    public void setListener(ItemLongClickedListener listener){
        mListener = listener;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG, "onResume()");

        //initDialogWindow(getDialog());
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Log.v(TAG,"onCreateDialog()");
        Dialog dialog = new Dialog(getContext(),R.style.FullScreeWidthDialog);

        dialog.setContentView(R.layout.item_photo_layout);

        initDialogWindow(dialog);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        View root = inflater.inflate(R.layout.item_image_detail,parent,false);

        initView((ImageView)root);

        root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String url = getArguments().getString(IMAGE_URL);
                if(mListener != null){
                    mListener.onImageLongClicked(url);
                }

                return true;
            }
        });

        return root;
    }


    private void initDialogWindow(Dialog dialog){
        Window window = dialog.getWindow();

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

    private void initView(ImageView iv){
        Log.v(TAG,"initView()");
        String url = getArguments().getString(IMAGE_URL);
        if(!TextUtils.isEmpty(url)) {
            GlideLoaderListener listener = new GlideLoaderListener(iv);
            GlideLoader.load(getContext(), url, listener,
                    Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
        }
    }
}
