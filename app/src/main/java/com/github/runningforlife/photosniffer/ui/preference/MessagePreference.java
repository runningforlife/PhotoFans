package com.github.runningforlife.photosniffer.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.github.runningforlife.photosniffer.R;

import java.io.File;

/**
 * Created by jason on 6/11/17.
 */

public class MessagePreference extends DialogPreference {
    private static final String TAG = "MessagePref";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MessagePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new DeleteRunnable(cacheDir))
                        .start();
            }
        });
    }

    private static class DeleteRunnable implements Runnable{
        private File file;

        public DeleteRunnable(File file){
            this.file = file;
        }

        @Override
        public void run() {
            if(file != null) {
                File[] all = file.listFiles();
                for (File file : all) {
                    file.delete();
                }
            }
        }
    }
}
