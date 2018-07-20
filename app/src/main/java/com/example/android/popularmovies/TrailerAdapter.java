package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.URL;

/**
 * Created by Khare on 01-Apr-18.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private static final String TAG = TrailerAdapter.class.getSimpleName();
    private int mNumberItems;
    Context mContext;
    private URL[] trailerPath;
    private LayoutInflater mInflater;
    private ItemTrailerClickListener mTrailerClickListener;

    TrailerAdapter(Context context) {
        mContext = context;
    }

    public void setTrailerData(URL[] data) {
        trailerPath = data;
        mNumberItems = trailerPath.length;
        notifyDataSetChanged();
    }

    @Override
    public TrailerAdapter.TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutIdForTrailerItem = R.layout.trailer_list_item;
        mInflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = mInflater.inflate(layoutIdForTrailerItem, parent, shouldAttachToParentImmediately);
        TrailerViewHolder viewHolder = new TrailerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TrailerAdapter.TrailerViewHolder holder, int position) {
        if(trailerPath != null) {
            URL url = trailerPath[position];
            holder.trailerItemView.setText("Trailer " + (position + 1));
        }
    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }


    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView trailerItemView;

        public TrailerViewHolder(View itemView) {
            super(itemView);

            trailerItemView = itemView.findViewById(R.id.trailerList_display_trailer);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mTrailerClickListener != null) {
                mTrailerClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public void setmTrailerClickListener(ItemTrailerClickListener clickListener) {
        mTrailerClickListener = clickListener;
    }
    // parent activity will implement this method to respond to click events
    public interface ItemTrailerClickListener {
        void onItemClick(View view, int position);
    }
}
