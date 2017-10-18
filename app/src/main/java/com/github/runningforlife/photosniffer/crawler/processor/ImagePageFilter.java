package com.github.runningforlife.photosniffer.crawler.processor;

import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.util.List;

/**
 * filter the page
 */

public class ImagePageFilter implements PageFilter {
    private  List<String> defSourceSite;

    public ImagePageFilter(){
        defSourceSite = SharedPrefUtil.getImageSource();
    }

    @Override
    public boolean accept(String url) {
        return defSourceSite.contains(url) && (!url.startsWith(ImageSource.POLA_IMAGE_URL_START)
                || !url.startsWith(ImageSource.POLA_IMAGE_URL_START_1));
    }
}
