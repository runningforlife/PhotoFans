package com.github.runningforlife.photosniffer.crawler.processor;

import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.util.List;

/**
 * filter the page
 */

public class SourcePageFilter implements PageFilter {
    private  List<String> defSourceSite;

    public SourcePageFilter(){
        defSourceSite = SharedPrefUtil.getImageSource();
    }

    @Override
    public boolean accept(String url) {
        return defSourceSite.contains(url);
    }
}
