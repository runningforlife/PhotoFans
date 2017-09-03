package com.github.runningforlife.photosniffer.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.github.runningforlife.photosniffer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * a full width dialog to show something to user
 */

public class RetrieveHintFragment extends DialogFragment{
    public static final String TAG = "RetrieveHintFragment";

    @BindView(R.id.ib_close) ImageButton ibClose;

    public static DialogFragment newInstance(){
        return  new RetrieveHintFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedState){
        Log.v(TAG,"onCreateView()");
        View root = inflater.inflate(R.layout.fragment_diaglog_refresh_hint, vg, false);

        ButterKnife.bind(this, root);

        initView();

        return root;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG, "onResume()");

        initDialog();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void initView(){

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initDialog(){
        Window window = getDialog().getWindow();

        if(window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.gravity = Gravity.TOP;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if(Build.VERSION.SDK_INT >= 21) {
                Transition transition = TransitionInflater.from(getContext())
                        .inflateTransition(android.R.transition.fade);
                window.setExitTransition(transition);
            }
        }
    }

}
