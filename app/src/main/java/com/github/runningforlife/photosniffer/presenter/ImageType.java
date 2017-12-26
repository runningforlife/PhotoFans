package com.github.runningforlife.photosniffer.presenter;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_FAVOR;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_GALLERY;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_WALLPAPER;

/**
 * Created by jason on 12/24/17.
 */

@Retention(RetentionPolicy.SOURCE)
@IntDef({IMAGE_GALLERY, IMAGE_FAVOR, IMAGE_WALLPAPER})
public @interface ImageType {
    int IMAGE_GALLERY = 0x11;
    int IMAGE_FAVOR = 0x12;
    int IMAGE_WALLPAPER = 0x13;
}
