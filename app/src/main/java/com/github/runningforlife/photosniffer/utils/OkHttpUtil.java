package com.github.runningforlife.photosniffer.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by jason on 18-3-31.
 */

public class OkHttpUtil {
    private static final int DEFAULT_READ_TIMEOUT = 10;
    private static final int DEFAULT_CONNECT_TIMEOUT = 10;

    public static OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS);

        return builder.build();
    }

    public static Request buildHttpRequest(String url) {
        Request.Builder builder = new Request.Builder();
        return builder.url(url)
                .build();
    }
}
