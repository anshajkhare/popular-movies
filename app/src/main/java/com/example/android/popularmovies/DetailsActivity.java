package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.utilites.DetailJsonUtils;
import com.example.android.popularmovies.utilites.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;

public class DetailsActivity extends AppCompatActivity
        implements TrailerAdapter.ItemTrailerClickListener {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    private final String KEY_TRAILER_RECYCLER_STATE = "trsiler_recycler_state";
    private final String KEY_REVIEW_RECYCLER_STATE = "review_recycler_state";
    private final String KEY_TITLE = "title";
    private final String KEY_YEAR = "year";
    private final String KEY_RATING = "rating";
    private final String KEY_PLOT = "plot";
    private final String KEY_URI = "uri";

    private ImageView movieImageView;
    private TextView movieTitleView;
    private TextView movieYearView;
    private TextView movieRatingView;
    private TextView moviePlotView;
    private String mMovieId;
    private RecyclerView mTrailerRecyclerView;
    private RecyclerView mReviewRecyclerView;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    private String[] reviewAuthors;
    private String[] reviewContent;
    private URL[] trailerUrls;
    private ProgressBar trailerProgressBar;
    private ProgressBar reviewProgressBar;
    private TextView trailerErrorMessageView;
    private TextView reviewErrorMessageView;
    private boolean setFavorite;
    private Button favoriteButton;
    private String uri;


    SharedPreferences favoritesPreference;
    Parcelable mTrailerRecyclerViewState;
    Parcelable mReviewRecyclerViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        favoritesPreference = getApplicationContext().getSharedPreferences("favoritesPreference", 0);

        movieImageView = findViewById(R.id.detail_image);
        movieTitleView = findViewById(R.id.detail_title);
        movieYearView = findViewById(R.id.detail_year);
        movieRatingView = findViewById(R.id.detail_rating);
        moviePlotView = findViewById(R.id.detail_plot);
        mTrailerRecyclerView = findViewById(R.id.rv_trailer_list);
        mReviewRecyclerView = findViewById(R.id.rv_review_list);
        trailerErrorMessageView = findViewById(R.id.trailer_error_message_display);
        reviewErrorMessageView = findViewById(R.id.review_error_message_display);
        trailerProgressBar = findViewById(R.id.trailer_pb_loading_indicator);
        reviewProgressBar = findViewById(R.id.review_pb_loading_indicator);
        favoriteButton = findViewById(R.id.toggle_favorites);


        LinearLayoutManager layoutManagerTrailer =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

       LinearLayoutManager layoutManagerReview =
               new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);


        mTrailerRecyclerView.setLayoutManager(layoutManagerTrailer);
        mReviewRecyclerView.setLayoutManager(layoutManagerReview);

        mTrailerRecyclerView.setHasFixedSize(true);
        mReviewRecyclerView.setHasFixedSize(true);
        mTrailerRecyclerView.setNestedScrollingEnabled(false);
        mReviewRecyclerView.setNestedScrollingEnabled(false);

        trailerAdapter = new TrailerAdapter(this);
        trailerAdapter.setmTrailerClickListener(this);
        reviewAdapter = new ReviewAdapter(this);

        mTrailerRecyclerView.setAdapter(trailerAdapter);
        mReviewRecyclerView.setAdapter(reviewAdapter);

        if(savedInstanceState != null) {
            movieTitleView.setText(savedInstanceState.getString(KEY_TITLE));
            movieRatingView.setText(savedInstanceState.getString(KEY_RATING));
            movieYearView.setText(savedInstanceState.getString(KEY_YEAR));
            moviePlotView.setText(savedInstanceState.getString(KEY_PLOT));
            Picasso.with(this).load(savedInstanceState.getString(KEY_URI)).into(movieImageView);
        }


        // Display the image that was passed from MainActivity
        Intent intent = getIntent();

        uri = intent.getStringExtra("imageURL");
        if(uri != null) {
            Picasso.with(this).load(uri).into(movieImageView);
        }
        String title = intent.getStringExtra("movieTitle");
        if (title != null) {
            movieTitleView.setText(title);
        }
        String year = intent.getStringExtra("movieYear");
        if (year != null) {
            movieYearView.setText(year);
        }
        String rating = intent.getStringExtra("movieRating");
        if (rating != null) {
            movieRatingView.setText(rating);
        }
        String plot = intent.getStringExtra("moviePlot");
        if (plot != null) {
            moviePlotView.setText(plot);
        }
        mMovieId = intent.getStringExtra("movieId");
        setFavorite = favoritesPreference.getBoolean(mMovieId, false);
        if(setFavorite) {
            favoriteButton.setText(R.string.remove_as_favorite);
        }
        else {
            favoriteButton.setText(R.string.mark_as_favorite);
        }
        loadTrailerData(mMovieId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String title = movieTitleView.getText().toString();
        String year = movieYearView.getText().toString();
        String rating = movieRatingView.getText().toString();
        String plot = moviePlotView.getText().toString();

        outState.putString(KEY_TITLE, title);
        outState.putString(KEY_YEAR, year);
        outState.putString(KEY_RATING, rating);
        outState.putString(KEY_PLOT, plot);
        outState.putString(KEY_URI, uri);

        mTrailerRecyclerViewState = mTrailerRecyclerView.getLayoutManager().onSaveInstanceState();
        mReviewRecyclerViewState = mReviewRecyclerView.getLayoutManager().onSaveInstanceState();

        outState.putParcelable(KEY_TRAILER_RECYCLER_STATE, mTrailerRecyclerViewState);
        outState.putParcelable(KEY_REVIEW_RECYCLER_STATE, mReviewRecyclerViewState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null) {
            mTrailerRecyclerViewState = savedInstanceState.getParcelable(KEY_TRAILER_RECYCLER_STATE);
            mReviewRecyclerViewState = savedInstanceState.getParcelable(KEY_REVIEW_RECYCLER_STATE);
        }
    }

    private void loadTrailerData(String mMovieId) {
        new FetchTrailerTask().execute(mMovieId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onItemClick(View view, int position) {
        URL url = trailerUrls[position];
        Intent yt_play = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        Intent chooser = Intent.createChooser(yt_play , "Open With");

        if (yt_play .resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    public void addMovieToFavorites(View view) {
        SharedPreferences.Editor editor = favoritesPreference.edit();
        if (setFavorite) {
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(mMovieId).build();
            String[] selectionArgs = new String[]{mMovieId};
            getContentResolver().delete(uri,null,selectionArgs);
            favoriteButton.setText(R.string.mark_as_favorite);
            editor.putBoolean(mMovieId, false);
            setFavorite = false;
        }
        else {
            Intent intent = getIntent();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovieId);
            contentValues.put(MovieContract.MovieEntry.COLUMN_NAME, intent.getStringExtra("movieTitle"));
            contentValues.put(MovieContract.MovieEntry.COLUMN_IMAGE_URL, intent.getStringExtra("imageURL"));
            contentValues.put(MovieContract.MovieEntry.COLUMN_YEAR, intent.getStringExtra("movieYear"));
            contentValues.put(MovieContract.MovieEntry.COLUMN_RATING, intent.getStringExtra("movieRating"));
            contentValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, intent.getStringExtra("moviePlot"));

            Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);
            if(uri != null) {
                Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
                favoriteButton.setText(R.string.remove_as_favorite);
                editor.putBoolean(mMovieId, true);
                setFavorite = true;
            }
        }
        editor.apply();
    }

    public class FetchTrailerTask extends AsyncTask<String, Void, URL[]> {

        @Override
        protected void onPreExecute() {
            trailerProgressBar.setVisibility(View.VISIBLE);
            reviewProgressBar.setVisibility(View.VISIBLE);
            mTrailerRecyclerView.setVisibility(View.INVISIBLE);
            mReviewRecyclerView.setVisibility(View.INVISIBLE);
            trailerErrorMessageView.setVisibility(View.INVISIBLE);
            reviewErrorMessageView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected URL[] doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            }
            String id = strings[0];
            URL trailerListRequestUrl = NetworkUtils.buildTrailerListUrl(id);
            URL reviewListRequestUrl = NetworkUtils.buildReviewListUrl(id);
            try {
                String jsonTrailerResponse = NetworkUtils.getResponseFromHttpUrl(trailerListRequestUrl);
                String jsonReviewResponse = NetworkUtils.getResponseFromHttpUrl(reviewListRequestUrl);
                DetailJsonUtils.getTrailerDetailsFromJson(DetailsActivity.this, jsonTrailerResponse);
                DetailJsonUtils.getReviewDetailsFromJson(DetailsActivity.this, jsonReviewResponse);
                return DetailJsonUtils.getMovieTrailerUrl();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(URL[] urls) {
            trailerProgressBar.setVisibility(View.INVISIBLE);
            reviewProgressBar.setVisibility(View.INVISIBLE);
            if(urls != null) {
                trailerUrls = urls;
                reviewAuthors = DetailJsonUtils.getMovieReviewAuthor();
                reviewContent = DetailJsonUtils.getMovieReviewContent();
                reviewAdapter.setReviewData(reviewAuthors, reviewContent);
                trailerAdapter.setTrailerData(trailerUrls);
                showData();
            }
            else {
                showErrorMessage();
            }
        }
    }

    private void showData() {
        trailerErrorMessageView.setVisibility(View.INVISIBLE);
        reviewErrorMessageView.setVisibility(View.INVISIBLE);
        mTrailerRecyclerView.setVisibility(View.VISIBLE);
        mReviewRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        trailerErrorMessageView.setVisibility(View.VISIBLE);
        reviewErrorMessageView.setVisibility(View.VISIBLE);
        mTrailerRecyclerView.setVisibility(View.INVISIBLE);
        mReviewRecyclerView.setVisibility(View.INVISIBLE);
    }
}
