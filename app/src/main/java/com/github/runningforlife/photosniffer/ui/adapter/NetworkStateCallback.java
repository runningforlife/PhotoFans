package com.github.runningforlife.photosniffer.ui.adapter;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jason on 10/21/17.
 */

public interface NetworkStateCallback {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({STATE_GOOD, STATE_SLOW, STATE_HUNG, STATE_DISCONNECT})
    @interface NetworkState{}

    String STATE_GOOD = "good";
    String STATE_SLOW = "slow";
    String STATE_HUNG = "hung";
    String STATE_DISCONNECT = "disconnect";

    /**
     * current network state
     */
    void onNetworkState(@NetworkState String state);
}
