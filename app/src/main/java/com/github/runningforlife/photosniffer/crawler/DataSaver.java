package com.github.runningforlife.photosniffer.crawler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;

import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImagePageInfo;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;
import com.github.runningforlife.photosniffer.utils.UrlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.runningforlife.photosniffer.service.ImageRetrieveService.EVENT_RETRIEVE_DONE;

/**
 * Created by jason on 18-3-24.
 */

public class DataSaver extends Handler {
    static final String TAG = "DataSaver";

    public static final int REQUEST_IMAGE_SAVE = 100;

    private HashMap<String,Boolean> mPagesState;
    private ExecutorService mSaverExecutor;
    private Handler mServiceHandler;
    private int mMaxData;
    private int mCurrentRetrievedImages;

    public DataSaver(Handler serviceHandler, int maxData, HashMap<String, Boolean> pageState, Looper looper) {
        super(looper);

        mMaxData = maxData;
        mServiceHandler = serviceHandler;
        mPagesState = pageState;
        mSaverExecutor = Executors.newSingleThreadExecutor();
        mCurrentRetrievedImages = 0;
    }

    public void setMaxRetrievedImages(int max) {
        mMaxData = max;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == REQUEST_IMAGE_SAVE) {
            mSaverExecutor.submit(new SaveRunnable((List<String>) msg.obj));
        }
    }

    private final class SaveRunnable implements Runnable {

        List<String> data;

        SaveRunnable(List<String> data) {
            this.data = data;
        }

        @Override
        public void run() {
            RealmApi realmApi = RealmApiImpl.getInstance();
            try {
                if (data != null && data.size() > 0 && UrlUtil.isPossibleImageUrl(data.get(0))) {
                    List<ImageRealm> realmObjects = new ArrayList<>(data.size());
                    for (String url : data) {
                        ImageRealm ir = new ImageRealm();
                        ir.setUrl(url);
                        if (DisplayUtil.getScreenDensity() >= DisplayMetrics.DENSITY_HIGH) {
                            ir.setHighResUrl(HighResImageUrlBuilder.buildHighResImageUrl(url, true));
                        } else {
                            ir.setHighResUrl(HighResImageUrlBuilder.buildHighResImageUrl(url, false));
                        }
                        realmObjects.add(ir);
                    }
                    realmApi.insertAsync(realmObjects);

                    mCurrentRetrievedImages += data.size();
                    if (mCurrentRetrievedImages > mMaxData) {
                        Message message = mServiceHandler.obtainMessage(EVENT_RETRIEVE_DONE);
                        message.obj = mCurrentRetrievedImages;
                        message.sendToTarget();
                        mCurrentRetrievedImages = Integer.MIN_VALUE;
                    }
                } else {
                    List<ImagePageInfo> pages = new ArrayList<>(data.size());
                    for (String url : data) {
                        ImagePageInfo pageInfo = new ImagePageInfo();
                        pageInfo.setUrl(url);
                        if (mPagesState.containsKey(url)) {
                            pageInfo.setIsVisited(mPagesState.get(url));
                        }
                        pages.add(pageInfo);
                    }
                    realmApi.insertAsync(pages);
                }
            } finally {
                realmApi.closeRealm();
            }
        }
    }
}
