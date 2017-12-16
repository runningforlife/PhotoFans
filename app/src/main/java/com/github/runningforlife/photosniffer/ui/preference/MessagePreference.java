package com.github.runningforlife.photosniffer.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jason on 6/11/17.
 */

public class MessagePreference extends DialogPreference {
    private static final String TAG = "MessagePref";

    private ExecutorService mDeletionExecutor;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MessagePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mDeletionExecutor = Executors.newSingleThreadExecutor();
    }

    public MessagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MessagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MessagePreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(android.app.AlertDialog.Builder builder) {
        // clear all caches
        final File cacheDir = Glide.getPhotoCacheDir(getContext());
        final File logDir = new File(MiscUtil.getLogDir());
        final File wallpaperDir = new File(MiscUtil.getWallpaperCacheDir());
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cacheDir.exists()) {
                    DeleteRunnable cache = new DeleteRunnable(cacheDir);
                    mDeletionExecutor.submit(cache);
                }
                if (logDir.exists()) {
                    DeleteRunnable log = new DeleteRunnable(logDir);
                    mDeletionExecutor.submit(log);
                }
                if (wallpaperDir.exists()) {
                    DeleteRunnable wallpaper = new DeleteRunnable(wallpaperDir);
                    mDeletionExecutor.submit(wallpaper);
                }
            }
        });
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();

        if (mDeletionExecutor.isTerminated()) {
            mDeletionExecutor.shutdown();
        }
    }

    private final class DeleteRunnable implements Runnable {
        private File file;

        DeleteRunnable(File file){
            this.file = file;
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
        }
    }
}
