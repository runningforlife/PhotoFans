package com.github.runningforlife.photosniffer.ui.fragment;

import android.os.Bundle;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;

import com.github.runningforlife.photosniffer.ui.activity.Refresh;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * a abstract fragment class implemented by child
 */

public class BaseFragment extends Fragment implements Refresh {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LinearManager,GridManager})
    public @interface RecycleLayout{}
    public static final String LinearManager = "linearManager";
    public static final String GridManager = "GridManager";
    // current context menu item view position
    protected int mCurrentPos = -1;

    public interface FragmentCallback {
        void onItemClick(int pos, String url);
        void onFragmentAttached();
        void showToast(String toast);
    }


    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        //setRetainInstance(true);
    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

    @Override
    public void setRefreshing(boolean enable) {
        // empty
    }

}
