package com.github.runningforlife.photosniffer.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.activity.UserAdviceActivity;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jason on 6/11/17.
 */

public class MessagePreference extends DialogPreference {
    private static final String TAG = "MessagePref";
    private static final int DELETION_WAIT_TIME_OUT = 10*1000;

    //private static final int DIR_CACHE = 0;
    private static final int DIR_LOG = 0;
    private static final int DIR_PHOTOS = 1;
    private static final int DIR_WALLPAPERS = 2;

    private boolean[] mDefaultSelectedFolder;

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
    public void onClick() {
        String prefAdvice = getContext().getString(R.string.pref_report_issue_and_advice);
        String prefCacheClear = getContext().getString(R.string.pref_cache_clear);
        String prefCopyRight = getContext().getString(R.string.pref_copyright);

        if (prefAdvice.equals(getKey())) {
            Intent intent = new Intent(getContext(), UserAdviceActivity.class);
            getContext().startActivity(intent);
        } else if (prefCacheClear.equals(getKey()) || prefCopyRight.equals(getKey())) {
            super.onClick();
        }
    }

    @Override
    protected void onPrepareDialogBuilder(android.app.AlertDialog.Builder builder) {
        String prefClearCache = getContext().getString(R.string.pref_cache_clear);
        if (prefClearCache.equals(getKey())) {
            // clear all caches
            final File logDir = new File(MiscUtil.getLogDir());
            final File photoDir = new File(MiscUtil.getPhotoDir());
            final File wallpaperDir = new File(MiscUtil.getWallpaperCacheDir());

            builder.setTitle(R.string.choose_the_cache_folder_to_delete)
                    .setMultiChoiceItems(R.array.cache_clear_folder, mDefaultSelectedFolder, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (mDefaultSelectedFolder[which] != isChecked) {
                        mDefaultSelectedFolder[which] = isChecked;
                    }
                }
            }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.show();

                    if (logDir.exists() && mDefaultSelectedFolder[DIR_LOG]) {
                        DeleteRunnable log = new DeleteRunnable(mLatch, logDir);
                        mDeletionExecutor.submit(log);
                    } else {
                        mLatch.countDown();
                    }
                    if (photoDir.exists() && mDefaultSelectedFolder[DIR_PHOTOS]) {
                        DeleteRunnable photo = new DeleteRunnable(mLatch, photoDir);
                        mDeletionExecutor.submit(photo);
                    } else {
                        mLatch.countDown();
                    }
                    if (wallpaperDir.exists() && mDefaultSelectedFolder[DIR_WALLPAPERS]) {
                        DeleteRunnable wallpaper = new DeleteRunnable(mLatch, wallpaperDir);
                        mDeletionExecutor.submit(wallpaper);

                        removeRealmData();
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
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDeletionExecutor != null && mDeletionExecutor.isTerminated()) {
            mDeletionExecutor.shutdown();
        }
    }

    private void init() {
        String prefClearCache = getContext().getString(R.string.pref_cache_clear);
        if (prefClearCache.equals(getKey())) {
            String[] clearedFolder = getContext().getResources().getStringArray(R.array.cache_clear_folder);
            mDefaultSelectedFolder = new boolean[clearedFolder.length];
            mDefaultSelectedFolder[DIR_LOG] = true;
            mDefaultSelectedFolder[DIR_PHOTOS] = mDefaultSelectedFolder[DIR_WALLPAPERS] = false;

            mDeletionExecutor = Executors.newSingleThreadExecutor();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(R.layout.item_file_deletion_alert);
            mAlertDialog = builder.create();

            mLatch = new CountDownLatch(clearedFolder.length);
        }
    }

    private void removeRealmData() {
        RealmApi realmApi = RealmApiImpl.getInstance();
        HashMap<String,String> params = new HashMap<>();
        params.put("mIsWallpaper", Boolean.toString(true));
        try {
            realmApi.deleteAsync(ImageRealm.class, params);
        } finally {
            realmApi.closeRealm();
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
