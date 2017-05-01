package jason.github.com.photofans.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jason.github.com.photofans.R;
import jason.github.com.photofans.model.ImageSource;

/**
 * a list adapter to bind data to list view
 */

public class MultiSelectionListAdapter extends BaseAdapter {
    private static final String TAG = "SelectionListAdapter";

    private List<ImageSource> mImgSource;
    private Set<String> mNewSource;
    private SelectionItemClickListener mCallback;
    private List<CheckBox> mCbs;

    public interface SelectionItemClickListener{
        void onLongClick(ImageSource src);
    }

    public MultiSelectionListAdapter(List<ImageSource> sources){
        mImgSource = sources;
        mCbs = new ArrayList<>(sources.size());
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

        View root = convertView;
        if(root == null){
            root = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selection_list_dialog,parent,false);

            final CheckBox cb = (CheckBox)root.findViewById(R.id.cb_select);
            TextView name = (TextView)root.findViewById(R.id.tv_site_name);
            TextView url = (TextView)root.findViewById(R.id.tv_site_url);

            String webSiteName = mImgSource.get(position).name;
            name.setText(webSiteName);
            url.setText(mImgSource.get(position).url);

            if(mNewSource.contains(webSiteName)){
                cb.setChecked(true);
            }else{
                cb.setChecked(false);
            }
            mCbs.add(position,cb);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked) {
                        int pos = mCbs.indexOf(cb);
                        mNewSource.add(mImgSource.get(pos).name);
                    }
                }
            });
        }

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox)v.findViewById(R.id.cb_select);
                boolean isChecked = cb.isChecked();
                cb.setChecked((!isChecked));

                if(cb.isChecked()) {
                    int pos = mCbs.indexOf(cb);
                    mNewSource.add(mImgSource.get(pos).name);
                }
            }
        });

        root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mCallback != null){
                    CheckBox cb = (CheckBox)v.findViewById(R.id.cb_select);
                    int pos = mCbs.indexOf(cb);
                    mCallback.onLongClick(mImgSource.get(pos));
                }
                // input consumed
                return true;
            }
        });

        return root;
    }
}
