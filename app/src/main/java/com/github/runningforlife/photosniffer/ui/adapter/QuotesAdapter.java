package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.PicassoLoader;
import com.github.runningforlife.photosniffer.model.QuoteRealm;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * adapter to binder quotes to view
 */

public class QuotesAdapter extends RecyclerView.Adapter<QuotesAdapter.QuoteViewHolder> {
    private static final String TAG = "QuotesAdapter";
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    private QuotesAdapterCallback callback;
    private Context context;

    public QuotesAdapter(Context context, @NonNull QuotesAdapterCallback callback){
        this.context = context;
        this.callback = callback;
    }

    @Override
    public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(TAG,"onCreateViewHolder()");

        View item = LayoutInflater.from(context).inflate(R.layout.item_quote, parent, false);

        return new QuoteViewHolder(item);
    }

    @Override
    public void onBindViewHolder(QuoteViewHolder holder, int position) {
        Log.v(TAG,"onBindViewHolder()");

        final QuoteRealm qr = (QuoteRealm)callback.getItemAtPos(position);

        holder.tvAuthor.setText(qr.getAuthor());
        holder.tvContent.setText(qr.getText());
        // loading header
        if(!TextUtils.isEmpty(qr.getHeader())) {
            PicassoLoader.load(context, holder.ivHeader, qr.getHeader(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }else{
            // default picture
            holder.ivHeader.setImageResource(R.mipmap.pic_unavailable);
        }
    }

    @Override
    public int getItemCount() {
        return callback.getCount();
    }

    final class QuoteViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener{
        @BindView(R.id.tv_author) TextView tvAuthor;
        @BindView(R.id.tv_content) TextView tvContent;
        @BindView(R.id.iv_header) ImageView ivHeader;

        QuoteViewHolder(final View view) {
            super(view);

            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback != null){
                        callback.onItemClicked(view, getAdapterPosition(), TAG);
                    }
                }
            });

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Log.v(TAG,"onCreateContextMenu()");

            MenuInflater inflater = ((AppCompatActivity)context).getMenuInflater();
            inflater.inflate(R.menu.menu_context_quote, menu);

            if(callback != null){
                callback.onContextMenuCreated(getAdapterPosition(), TAG);
            }
        }
    }
}
