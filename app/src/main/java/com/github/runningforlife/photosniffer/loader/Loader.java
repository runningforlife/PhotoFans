package com.github.runningforlife.photosniffer.loader;

import android.support.annotation.StringDef;

import com.github.runningforlife.photosniffer.utils.DisplayUtil;

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

    // large image size, used for full screen image
    int DEFAULT_IMG_WIDTH = (DisplayUtil.getScreenDimen().widthPixels);
    int DEFAULT_IMG_HEIGHT = (int)(DEFAULT_IMG_WIDTH/DisplayUtil.getScreenRatio());
    // medium image size
    int DEFAULT_IMAGE_MEDIUM_WIDTH = DEFAULT_IMG_WIDTH/2;
    int DEFAULT_IMAGE_MEDIUM_HEIGHT = (int)(DEFAULT_IMAGE_MEDIUM_WIDTH*DisplayUtil.getScreenRatio());

}
