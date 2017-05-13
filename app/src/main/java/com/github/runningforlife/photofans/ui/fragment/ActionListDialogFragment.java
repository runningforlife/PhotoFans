package com.github.runningforlife.photofans.ui.fragment;

import android.app.Dialog;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.runningforlife.photofans.R;

import java.util.ArrayList;
import java.util.List;

/**
 * a dialog fragment to show a list of action
 */

public class ActionListDialogFragment extends DialogFragment{
    private static final String TAG = "ActionList";
    private List<String> mActionList;
    private ActionCallback mCb;

    public interface ActionCallback{
        void onActionClick(String action, int pos);
    }

    public ActionListDialogFragment(){
    }

    public static DialogFragment newInstance(List<String> actions){
        ActionListDialogFragment fragment = new ActionListDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("action_list", new ArrayList<String>(actions));

        fragment.setArguments(args);

        return fragment;
    }

    public void setCallback(ActionCallback cb){
        mCb = cb;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG,"onStart()");
        initDialogWindow(getDialog());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        Log.v(TAG,"onCreateView()");
        View root = inflater.inflate(R.layout.fragment_dialog_action_list,parent,false);

        initActionView(root);

        return root;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Log.v(TAG,"onCreateDialog()");
        Dialog dialog = new Dialog(getContext(),R.style.FullScreeWidthDialog);

        dialog.setContentView(R.layout.fragment_dialog_action_list);
        return dialog;
    }

    private void initDialogWindow(Dialog dialog){
        Window window = dialog.getWindow();

        if(window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.gravity = Gravity.BOTTOM;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void initActionView(View root){
        Bundle args = getArguments();

        if(args != null){
            mActionList = args.getStringArrayList("action_list");

            ListView lv = (ListView)root.findViewById(R.id.lv_actions);
            lv.setAdapter(new ActionListAdapter());
        }
    }


    private final class ActionListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mActionList.size();
        }

        @Override
        public Object getItem(int position) {
            return mActionList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null){
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_dialog_action,parent,false);
                TextView tv = (TextView)view.findViewById(R.id.tv_action);

                final String action = mActionList.get(position);
                tv.setText(action);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mCb != null){
                            mCb.onActionClick(action,position);
                        }
                    }
                });
            }

            return view;
        }
    }
}
