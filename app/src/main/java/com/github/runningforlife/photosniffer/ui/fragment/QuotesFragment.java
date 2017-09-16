package com.github.runningforlife.photosniffer.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.presenter.QuotePresenter;
import com.github.runningforlife.photosniffer.presenter.QuotePresenterImpl;
import com.github.runningforlife.photosniffer.ui.QuoteView;
import com.github.runningforlife.photosniffer.ui.adapter.QuotesAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.QuotesAdapterCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmObject;

/**
 * a fragment to show quotes
 */

public class QuotesFragment extends BaseFragment
        implements QuotesAdapterCallback, QuoteView {
    public static final String TAG = "QuotesFragment";

    @BindView(R.id.rcv_quotes) RecyclerView rcvQuote;
    private QuotesAdapter adapter;
    private QuotePresenter presenter;
    // current selected position to show context menu
    private int currentMenuPos;

    public static QuotesFragment newInstance(){
        return new QuotesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedState){
        Log.v(TAG,"onCreateView()");
        View root = inflater.inflate(R.layout.fragment_quotes, vg, false);

        ButterKnife.bind(this, root);

        initView();

        initPresenter();

        return root;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG,"onResume()");
        presenter.onStart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void onContextMenuCreated(int pos, String adapter) {
        Log.v(TAG,"onContextMenuCreated()");
        currentMenuPos = pos;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.v(TAG, "onContextItemSelected()");

        final  int id = item.getItemId();
        switch (id){
            case R.id.menu_share:
                break;
            case R.id.menu_favor:
                presenter.favorQuote(currentMenuPos);
                break;
            case R.id.menu_delete:
                presenter.removeItemAtPos(currentMenuPos);
                break;
        }

        return super.onContextItemSelected(item);
    }

        @Override
    public int getCount() {
        return presenter.getItemCount();
    }

    @Override
    public RealmObject getItemAtPos(int pos) {
        return presenter.getItemAtPos(pos);
    }

    @Override
    public void onItemClicked(View view, int pos, String adapter) {
        Log.v(TAG,"onItemClick()");
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.v(TAG,"removeItemAtPos()");
        presenter.removeItemAtPos(pos);
    }

    private void initView(){
        Log.v(TAG,"initView()");
        adapter = new QuotesAdapter(getContext(), this);

        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        llm.setSmoothScrollbarEnabled(true);
        llm.setAutoMeasureEnabled(true);
        rcvQuote.setLayoutManager(llm);
        rcvQuote.setHasFixedSize(true);

        rcvQuote.setAdapter(adapter);
    }

    private void initPresenter(){
        Log.v(TAG,"initPresenter()");

        presenter = new QuotePresenterImpl(getContext(), this);
        presenter.init();
    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG,"onDataSetChanged()");
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefreshDone(boolean isSuccess) {
        Log.v(TAG,"onRefreshDone()");


    }
}
