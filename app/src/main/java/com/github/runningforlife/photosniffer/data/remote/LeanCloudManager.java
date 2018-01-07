package com.github.runningforlife.photosniffer.data.remote;

import android.os.Build;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * lean cloud manager
 */

public class LeanCloudManager implements CloudApi{
    private static final String TAG = "LeanCloudMgr";

    private static final LeanCloudManager sInstance = new LeanCloudManager();

    public static LeanCloudManager getInstance(){
        return sInstance;
    }

    @Override
    public void saveFile(String name,String data) {
        Log.v(TAG, "saveFile()");
        AVFile file = new AVFile(name, data.getBytes());
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if(e != null){
                    Log.d(TAG,"saveFile(): fail");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void saveFile(final File file) {
        String name = "log_" + System.currentTimeMillis() + ".txt";
        try {
            AVFile cf = AVFile.withAbsoluteLocalPath(name, file.getAbsolutePath());
            cf.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if(e != null){
                        Log.d(TAG,"saveFile(): fail");
                        e.printStackTrace();
                    }else{
                        Log.v(TAG,"saveFile(): success");
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAdvice(String advice) {
        Log.v(TAG,"saveAdvice()");

        AVObject ao = new AVObject("UserAdvice");
        ao.put("brand", Build.BRAND);
        ao.put("device", Build.MODEL);
        ao.put("os version", Build.VERSION.RELEASE);
        ao.put("advice", advice);

        ao.saveInBackground();
    }

    @Override
    public void newUser(String info) {
        Log.v(TAG,"newUser()");

        AVObject ao = new AVObject(("UserInfo"));
        ao.put("brand", Build.BRAND);
        ao.put("device", Build.MODEL);
        ao.put("os version", Build.VERSION.RELEASE);
        ao.put("device fingerprint", info);

        ao.saveInBackground();
    }
}
