package com.github.runningforlife.photosniffer.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jason on 6/11/17.
 */

public class MessagePreference extends DialogPreference {
    private static final String TAG = "MessagePref";
    private static final int DELETION_WAIT_TIME_OUT = 10*1000;
    private ExecutorService mDeletionExecutor;
    // a count down latch to wait for file deletion complete
    private CountDownLatch mLatch;
    private AlertDialog mAlertDialog;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MessagePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    public MessagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public MessagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MessagePreference(Context context) {
        super(context);

        init();
    }

    @Override
    protected void onPrepareDialogBuilder(android.app.AlertDialog.Builder builder) {
        String prefClearCache = getContext().getString(R.string.pref_cache_clear);
        if (prefClearCache.equals(getKey())) {
            // clear all caches
            final File cacheDir = Glide.getPhotoCacheDir(getContext());
            final File logDir = new File(MiscUtil.getLogDir());
            final File wallpaperDir = new File(MiscUtil.getWallpaperCacheDir());
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.show();

                    if (cacheDir.exists()) {
                        DeleteRunnable cache = new DeleteRunnable(mLatch, cacheDir);
                        mDeletionExecutor.submit(cache);
                    } else {
                        mLatch.countDown();
                    }
                    if (logDir.exists()) {
                        DeleteRunnable log = new DeleteRunnable(mLatch, logDir);
                        mDeletionExecutor.submit(log);
                    } else {
                        mLatch.countDown();
                    }
                    if (wallpaperDir.exists()) {
                        DeleteRunnable wallpaper = new DeleteRunnable(mLatch, wallpaperDir);
                        mDeletionExecutor.submit(wallpaper);
                    } else {
                        mLatch.countDown();
                    }

                    try {
                        mLatch.await(DELETION_WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mAlertDialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();

        if (mDeletionExecutor.isTerminated()) {
            mDeletionExecutor.shutdown();
        }
    }

    private void init() {
        String prefClearCache = getContext().getString(R.string.pref_cache_clear);
        if (prefClearCache.equals(getKey())) {

            mDeletionExecutor = Executors.newSingleThreadExecutor();
            mLatch = new CountDownLatch(3);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(R.layout.item_file_deletion_alert);
            mAlertDialog = builder.create();
        }
    }

    private final class DeleteRunnable implements Runnable {
        private File file;
        private CountDownLatch latch;

        DeleteRunnable(CountDownLatch latch, File file){
            this.file = file;
            this.latch = latch;
        }

        @Override
        public void run() {
            if(file != null) {
                File[] all = file.listFiles();
                for (File file : all) {
                    if(!file.delete()) {
                        Log.e(TAG,"fail to remove file=" + file.getAbsolutePath());
                    }
                }
            }

            latch.countDown();
        }
    }
}
