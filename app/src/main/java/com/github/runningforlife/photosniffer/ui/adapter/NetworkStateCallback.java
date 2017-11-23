package com.github.runningforlife.photosniffer.ui.adapter;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jason on 10/21/17.
 */

public interface NetworkStateCallback {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({STATE_GOOD, STATE_SLOW, STATE_HUNG})
    public @interface NetworkState{}

    public static final String STATE_GOOD = "good";
    public static final String STATE_SLOW = "slow";
    public static final String STATE_HUNG = "hung";

    /**
     * current network state
     */
    void onNetworkState(@NetworkState String state);
}
