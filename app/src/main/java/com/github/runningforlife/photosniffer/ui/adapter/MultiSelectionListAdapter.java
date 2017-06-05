package com.github.runningforlife.photosniffer.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.ImageWebSite;

/**
 * a list adapter to bind data to list view
 */

public class MultiSelectionListAdapter extends BaseAdapter {
    private static final String TAG = "SelectionListAdapter";

    private List<ImageWebSite> mImgSource;
    private Set<String> mNewSource;
    private SelectionItemClickListener mCallback;
    private HashMap<CheckBox,Integer> mCbs;

    public interface SelectionItemClickListener{
        void onLongClick(ImageWebSite src);
    }

    public MultiSelectionListAdapter(List<ImageWebSite> sources){
        mImgSource = sources;
        mCbs = new HashMap<>();
    }

    public void setDefaultSource(List<String> defaultSource){
        mNewSource = new HashSet<>(defaultSource);
    }

    public void setCallback(SelectionItemClickListener callback){
        mCallback = callback;
    }

    public List<String> getImageSource(){
        return new ArrayList<>(mNewSource);
    }

    @Override
    public int getCount() {
        return mImgSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mImgSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.v(TAG,"getView(): position = " + position);

        View root = convertView;
        if(root == null){
            root = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selection_list_dialog,parent,false);
        }

        final CheckBox cb = (CheckBox)root.findViewById(R.id.cb_select);
        mCbs.put(cb,position);

        TextView name = (TextView)root.findViewById(R.id.tv_site_name);
        TextView url = (TextView)root.findViewById(R.id.tv_site_url);

        name.setText(mImgSource.get(position).name);
        String srcUrl = mImgSource.get(position).url;
        url.setText(srcUrl);

        Log.d(TAG,"getView(): position = " + position + ", source = " + srcUrl);

        if(mNewSource.contains(srcUrl)){
            cb.setChecked(true);
        }else{
            cb.setChecked(false);
        }

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = mCbs.get(cb);
                String siteUrl = mImgSource.get(pos).url;
                if(isChecked) {
                    mNewSource.add(siteUrl);
                } else {
                    mNewSource.remove(siteUrl);
                }
            }
        });

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox)v.findViewById(R.id.cb_select);
                boolean isChecked = cb.isChecked();
                cb.setChecked((!isChecked));
/*
                if(cb.isChecked()) {
                    int pos = mCbs.get(cb);
                    mNewSource.add(mImgSource.get(pos).url);
                }*/
            }
        });

        root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mCallback != null){
                    CheckBox cb = (CheckBox)v.findViewById(R.id.cb_select);
                    int pos = mCbs.get(cb);
                    mCallback.onLongClick(mImgSource.get(pos));
                }
                // input consumed
                return true;
            }
        });

        return root;
    }
}
