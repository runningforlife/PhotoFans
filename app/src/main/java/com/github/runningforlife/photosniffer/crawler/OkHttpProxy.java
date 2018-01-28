package com.github.runningforlife.photosniffer.crawler;

/*
* a factory class to generate http client　<br>
* @author JasonWang
* @date 2017-3-12
*/

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.proxy.Proxy;

class OkHttpProxy {

    private static final int CONNECT_TIMEOUT = 5000;

    private final static OkHttpClient.Builder builder = new OkHttpClient.Builder();

    static OkHttpClient getClient(Site site, Proxy proxy){
        return builder
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(site.getTimeOut(), TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool())
                .retryOnConnectionFailure(true)
                .build();
    }
}
