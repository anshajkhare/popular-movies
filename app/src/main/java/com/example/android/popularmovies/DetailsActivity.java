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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.pojo.MovieData;
import com.example.android.popularmovies.pojo.ReviewData;
import com.example.android.popularmovies.pojo.ReviewResponse;
import com.example.android.popularmovies.pojo.TrailerData;
import com.example.android.popularmovies.pojo.TrailerResponse;
import com.example.android.popularmovies.utilites.DetailJsonUtils;
import com.example.android.popularmovies.utilites.MovieApiCallInterface;
import com.example.android.popularmovies.utilites.MovieApiModule;
import com.example.android.popularmovies.utilites.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity
        implements TrailerAdapter.ItemTrailerClickListener {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    static final String WATCH_QUERY = "v";

    private final String KEY_TRAILER_RECYCLER_STATE = "trailer_recycler_state";
    private final String KEY_REVIEW_RECYCLER_STATE = "review_recycler_state";
    private final String KEY_TITLE = "title";
    private final String KEY_YEAR = "year";
    private final String KEY_RATING = "rating";
    private final String KEY_PLOT = "plot";
    private final String KEY_URI = "uri";
    private Uri uri;

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
    private MovieData movieData;

    private List<TrailerData> trailerData;
    private List<ReviewData> reviewData;

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

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            movieData = bundle.getParcelable(MainActivity.MOVIE_ITEM_DATA_BUNDLE);
            if (movieData != null) {
                String path = movieData.getPosterPath();
                uri = Uri.parse(MovieApiModule.IMAGE_BASE_URL).buildUpon()
                        .appendEncodedPath(path)
                        .build();
                Picasso.with(this).load(uri).into(movieImageView);
                movieTitleView.setText(movieData.getTitle());
                movieYearView.setText(movieData.getReleaseDate());
                movieRatingView.setText(String.valueOf(movieData.getVoteAverage()));
                moviePlotView.setText(movieData.getOverview());
                mMovieId = String.valueOf(movieData.getId());
                setFavorite = favoritesPreference.getBoolean(mMovieId, false);
                if (setFavorite) {
                    favoriteButton.setText(R.string.remove_as_favorite);
                } else {
                    favoriteButton.setText(R.string.mark_as_favorite);
                }
                loadTrailerData(mMovieId);
            }
        }
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
        outState.putString(KEY_URI, uri.toString());

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
        MovieApiCallInterface movieApiCallInterface =
                MovieApiModule.getResponse().create(MovieApiCallInterface.class);
        Call<TrailerResponse> trailerCall =
                movieApiCallInterface.getTrailers(mMovieId, MovieApiModule.API_KEY_VALUE);
        trailerCall.enqueue(new Callback<TrailerResponse>() {
            @Override
            public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                trailerData = response.body().getTrailerData();
                trailerAdapter.setTrailerData(trailerData);
                trailerErrorMessageView.setVisibility(View.INVISIBLE);
                mTrailerRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<TrailerResponse> call, Throwable t) {
                trailerErrorMessageView.setVisibility(View.VISIBLE);
                mTrailerRecyclerView.setVisibility(View.INVISIBLE);
                Log.e(TAG, t.toString());
            }
        });
        Call<ReviewResponse> reviewCall =
                movieApiCallInterface.getReviews(mMovieId, MovieApiModule.API_KEY_VALUE);
        reviewCall.enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                reviewData = response.body().getReviewData();
                reviewAdapter.setReviewData(reviewData);
                reviewErrorMessageView.setVisibility(View.INVISIBLE);
                mReviewRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                reviewErrorMessageView.setVisibility(View.VISIBLE);
                mReviewRecyclerView.setVisibility(View.INVISIBLE);
                Log.e(TAG, t.toString());
            }
        });
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
        String key = trailerData.get(position).getKey();
        Uri uri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(WATCH_QUERY, key)
                .build();

        Intent yt_play = new Intent(Intent.ACTION_VIEW, uri);
        Intent chooser = Intent.createChooser(yt_play, "Open With");

        if (yt_play.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    public void addMovieToFavorites(View view) {
        int number = favoritesPreference.getInt("favoritesNumber", 0);
        SharedPreferences.Editor editor = favoritesPreference.edit();
        if (setFavorite) {
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(mMovieId).build();
            String[] selectionArgs = new String[]{mMovieId};
            getContentResolver().delete(uri, null, selectionArgs);
            favoriteButton.setText(R.string.mark_as_favorite);
            editor.putBoolean(mMovieId, false);
            editor.putInt("favoritesNumber", number - 1);
            setFavorite = false;
        }
        else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovieId);
            contentValues.put(MovieContract.MovieEntry.COLUMN_NAME, movieData.getTitle());
            contentValues.put(MovieContract.MovieEntry.COLUMN_IMAGE_URL, movieData.getPosterPath());
            contentValues.put(MovieContract.MovieEntry.COLUMN_YEAR, movieData.getReleaseDate());
            contentValues.put(MovieContract.MovieEntry.COLUMN_RATING, String.valueOf(movieData.getVoteAverage()));
            contentValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movieData.getOverview());

            Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);
            if(uri != null) {
                Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
                favoriteButton.setText(R.string.remove_as_favorite);
                editor.putBoolean(mMovieId, true);
                editor.putInt("favoritesNumber", number + 1);
                setFavorite = true;
            }
        }
        editor.apply();
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
