package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.pojo.ReviewData;

import java.util.List;

/**
 * Created by Khare on 01-Apr-18.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private static final String TAG = ReviewAdapter.class.getSimpleName();
    private int mNumberItems;
    private Context mContext;
    /*private String[] authorNames;
    private String[] reviewContent;*/
    private List<ReviewData> reviewData;
    private LayoutInflater mInflater;

    ReviewAdapter(Context context) {
        mContext = context;
    }

    public void setReviewData(List<ReviewData> data) {
        /*authorNames = authors;
        reviewContent = content;*/
        reviewData = data;
        if (reviewData != null)
            mNumberItems = reviewData.size();
        else
            mNumberItems = 0;
        notifyDataSetChanged();
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutIdForTrailerItem = R.layout.review_list_item;
        mInflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = mInflater.inflate(layoutIdForTrailerItem, parent, shouldAttachToParentImmediately);
        ReviewViewHolder reviewViewHolder = new ReviewViewHolder(view);

        return reviewViewHolder;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        if (reviewData != null) {
            ReviewData reviewContent = reviewData.get(position);
            if (reviewContent != null) {
                String review = reviewContent.getContent();
                String author = reviewContent.getAuthor();
                holder.reviewContentView.setText(review);
                holder.authorNameView.setText(author);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView authorNameView;
        TextView reviewContentView;


        public ReviewViewHolder(View itemView) {
            super(itemView);
            authorNameView = itemView.findViewById(R.id.reviewList_display_author);
            reviewContentView = itemView.findViewById(R.id.reviewList_display_content);
        }
    }
}
