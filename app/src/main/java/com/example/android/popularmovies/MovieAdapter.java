package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.pojo.MovieData;
import com.example.android.popularmovies.utilites.MovieApiModule;
import com.example.android.popularmovies.utilites.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

/**
 * Adapter class to bind the images for each movie
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ImageViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();
    private int mNumberItems;
    private List<MovieData> moviesList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Cursor mCursor;

    // data is passed into the constructor
    MovieAdapter(int numberOfItems) {
        mNumberItems = numberOfItems;
    }

    public void setImageData(List<MovieData> data) {
        moviesList = data;
        notifyDataSetChanged();
    }

    // inflates the cell layout from xml when needed
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForImageItem = R.layout.image_list_item;
        mInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = mInflater.inflate(layoutIdForImageItem, parent, shouldAttachToParentImmediately);
        ImageViewHolder viewHolder = new ImageViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        if (moviesList != null) {
            MovieData movieData = moviesList.get(position);
            if (MainActivity.getCurrentSelection() != MainActivity.OPTION_FAVORITES) {
                String path = movieData.getPosterPath();
                if (path != null) {
                    Uri uri = Uri.parse(MovieApiModule.IMAGE_BASE_URL).buildUpon()
                            .appendEncodedPath(path)
                            .build();
                    Picasso.with(holder.imageItemView.getContext()).load(uri).into(holder.imageItemView);
                }
            } else {
                mCursor.moveToPosition(position);
                String path = mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMAGE_URL));
                Uri uri = Uri.parse(MovieApiModule.IMAGE_BASE_URL).buildUpon()
                        .appendEncodedPath(path)
                        .build();
                String url = uri.toString();
                Picasso.with(holder.imageItemView.getContext()).load(url).into(holder.imageItemView);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (MainActivity.getCurrentSelection() != MainActivity.OPTION_FAVORITES) {
            return mNumberItems;
        }
        else {
            if (mCursor == null) {
                return 0;
            }
            return mCursor.getCount();
        }
    }

    public Cursor swapCursor(Cursor data) {

        if(mCursor == data) {
            return null;
        }
        Cursor temp = mCursor;
        this.mCursor = data; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (data != null) {
            if (MainActivity.getCurrentSelection() == MainActivity.OPTION_FAVORITES) {
                this.notifyDataSetChanged();
            }
        }
        return temp;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageItemView;

        public ImageViewHolder(View itemView) {
            super(itemView);

            imageItemView = itemView.findViewById(R.id.imagelist_display_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mClickListener != null) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public void setmClickListener(ItemClickListener clickListener) {
        mClickListener = clickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
