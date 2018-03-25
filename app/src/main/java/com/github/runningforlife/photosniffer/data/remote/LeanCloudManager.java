package com.github.runningforlife.photosniffer.data.remote;

import android.os.Build;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * lean cloud manager
 */

public class LeanCloudManager implements CloudApi {
    private static final String TAG = "LeanCloudMgr";

    private static final LeanCloudManager sInstance = new LeanCloudManager();

    public static LeanCloudManager getInstance() {
        return sInstance;
    }

    @Override
    public void saveFile(final File file) {
        String name = "log_" + System.currentTimeMillis() + ".txt";
        try {
            AVFile cf = AVFile.withAbsoluteLocalPath(name, file.getAbsolutePath());
            cf.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e != null) {
                        Log.d(TAG,"saveFile(): fail");
                        e.printStackTrace();
                    } else {
                        // remove logs locally
                        file.delete();
                        Log.v(TAG,"saveFile(): success");
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAdvice(String email, String advice, SaveCallback saveCallback) {
        Log.v(TAG,"saveAdvice()");

        AVObject ao = new AVObject("UserAdvice");
        ao.put("brand", Build.BRAND);
        ao.put("device", Build.MODEL);
        ao.put("osVersion", Build.VERSION.RELEASE);
        ao.put("email", email);
        ao.put("advice", advice);

        ao.saveInBackground(saveCallback);
    }

    @Override
    public void newUser() {
        Log.v(TAG,"newUser()");

        AVObject ao = new AVObject(("UserInfo"));
        ao.put("brand", Build.BRAND);
        ao.put("device", Build.MODEL);
        ao.put("osVersion", Build.VERSION.RELEASE);
        ao.put("fingerprint", Build.FINGERPRINT);

        ao.saveInBackground();
    }

    @Override
    public void savePolaCollections(int number) {
        Log.v(TAG,"savePolaCollections()");

        final AVObject ao = new AVObject("PolaCollections");
        ao.put("collections", number);

        ao.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    SharedPrefUtil.putString("pola_collections_id", ao.getObjectId());
                }
            }
        });
    }

    @Override
    public void getPolaCollections(String objectID, final GetDataCallback callback) {
        Log.v(TAG,"getPolaCollections()");

        AVQuery<AVObject> avQuery = new AVQuery<>("PolaCollections");
        avQuery.getInBackground(objectID, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    callback.onQueryPolaCollectionsDone(avObject.getInt("collections"));
                } else {
                    callback.onQueryPolaCollectionsDone(0);
                }
            }
        });
    }
}
