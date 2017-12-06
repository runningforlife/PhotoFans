package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.ui.activity.Refresh;
import com.github.runningforlife.photosniffer.ui.adapter.NetworkStateCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * a abstract fragment class implemented by child
 */

public class BaseFragment extends Fragment implements Refresh, NetworkStateCallback {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LinearManager,GridManager})
    public @interface RecycleLayout{}
    public static final String LinearManager = "linearManager";
    public static final String GridManager = "GridManager";
    // current context menu item view position
    protected int mCurrentPos = -1;
    protected FragmentCallback mCallback;

    public interface FragmentCallback {
        void onItemClick(View view, int pos, String url);
        void onFragmentAttached();
        void showToast(String toast);
    }


    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        //setRetainInstance(true);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        //GlideLoader.pauseRequest(getContext());
    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

    @Override
    public void setRefreshing(boolean enable) {
        // empty
    }

    protected void setTitle(String title){
        Activity activity = getActivity();
        if(activity != null){
            activity.setTitle(title);
        }
    }

    @Override
    public void onNetworkState(@NetworkState String state) {
        Log.v(TAG,"onNetworkState():state = " + state);
        if (mCallback != null) {
            switch (state) {
                case NetworkStateCallback.STATE_DISCONNECT:
                    mCallback.showToast(getString(R.string.network_not_connected));
                    break;
                case NetworkStateCallback.STATE_HUNG:
                    mCallback.showToast(getString(R.string.hint_network_state_hung));
                    break;
                case NetworkStateCallback.STATE_SLOW:
                    mCallback.showToast(getString(R.string.hint_network_state_slow));
                    break;
                default:
                    break;
            }
        }
    }
}
