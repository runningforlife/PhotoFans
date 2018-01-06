package com.github.runningforlife.photosniffer.ui.fragment;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_DELETE;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_FAVOR;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_SAVE_AS_WALLPAPER;

/**
 * Created by jason on 1/5/18.
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({BATCH_FAVOR, BATCH_DELETE, BATCH_SAVE_AS_WALLPAPER})
public @interface BatchAction {
    String BATCH_FAVOR = "favor";
    String BATCH_DELETE = "delete";
    String BATCH_SAVE_AS_WALLPAPER = "save";
}
