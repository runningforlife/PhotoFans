package com.github.runningforlife.photofans.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * a abstract fragment class implemented by child
 */

public class BaseFragment extends Fragment implements Refresh{


    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        setRetainInstance(true);
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