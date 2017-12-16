package com.github.runningforlife.photosniffer.presenter;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.runningforlife.photosniffer.presenter.NetState.*;

/**
 * network state
 */

@Retention(RetentionPolicy.SOURCE)
@StringDef({STATE_GOOD, STATE_SLOW, STATE_HUNG, STATE_DISCONNECT})
public @interface NetState{

    String STATE_GOOD = "good";
    String STATE_SLOW = "slow";
    String STATE_HUNG = "hung";
    String STATE_DISCONNECT = "disconnect";
}
