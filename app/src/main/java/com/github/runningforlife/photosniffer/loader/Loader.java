package com.github.runningforlife.photosniffer.loader;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jason on 6/10/17.
 */

public interface Loader {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PICASSO,GLIDE})
    @interface LOADER{}

    String PICASSO = "picasso";
    String GLIDE = "glide";
}
