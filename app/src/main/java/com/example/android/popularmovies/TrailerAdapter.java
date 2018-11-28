package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.pojo.TrailerData;

import java.net.URL;
import java.util.List;

/**
 * Created by Khare on 01-Apr-18.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private static final String TAG = TrailerAdapter.class.getSimpleName();
    private int mNumberItems;
    Context mContext;
    //private URL[] trailerPath;
    private List<TrailerData> trailerData;
    private LayoutInflater mInflater;
    private ItemTrailerClickListener mTrailerClickListener;

    TrailerAdapter(Context context) {
        mContext = context;
    }

    public void setTrailerData(List<TrailerData> data) {
        trailerData = data;
        if (trailerData != null)
            mNumberItems = trailerData.size();
        else
            mNumberItems = 0;
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
        if (trailerData != null) {
            String name = trailerData.get(position).getName();
            if (name != null) {
                String text = "Trailer " + (position + 1) + " " + name;
                holder.trailerItemView.setText(text);
            }
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
