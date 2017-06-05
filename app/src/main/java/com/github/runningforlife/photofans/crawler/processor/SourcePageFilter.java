package com.github.runningforlife.photofans.crawler.processor;

import com.github.runningforlife.photofans.utils.SharedPrefUtil;

import java.util.List;

/**
 * filter the page
 */

public class SourcePageFilter implements PageFilter {
    private  List<String> defSourceSite;

    SourcePageFilter(){
        defSourceSite = SharedPrefUtil.getImageSource();
    }

    @Override
    public boolean accept(String url) {
        return defSourceSite.contains(url);
    }
}
