package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @BugFix fix invalid view holder position
 * https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
 */

public class WrapperGridLayoutManager extends GridLayoutManager {
    private static final String TAG = WrapperGridLayoutManager.class.getSimpleName();

    public WrapperGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public WrapperGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public WrapperGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG,"index out of bound exception" + e);
        }
    }
}
