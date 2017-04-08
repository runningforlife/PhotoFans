package jason.github.com.photofans.ui;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.model.RealmHelper;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl implements ImageDetailPresenter{
    private static final String TAG = "ImagePreviewPresenter";

    private List<ImageRealm> mImgList;
    private ImageDetailView mView;
    private RealmHelper mHelper;

    public ImageDetailPresenterImpl(Context context, ImageDetailView view){
        mView = view;
        mImgList = new ArrayList<>();
        mHelper = RealmHelper.getInstance();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImgList.get(pos);
    }

    @Override
    public int getItemCount() {
        return mImgList.size();
    }

    @Override
    public void init() {
        mHelper.addListener(this);
        mHelper.queryAllAsync();
    }

    @Override
    public void onDestroy() {
        mHelper.removeListener(this);
        mHelper.onDestroy();
    }

    @Override
    public void onRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onRealmDataChange(): data size = " + data.size());

        for(ImageRealm img : data){
            if(!mImgList.contains(img)){
                mImgList.add(img);
            }
        }

        mView.onDataSetChanged();
    }
}
