package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.pojo.MovieData;
import com.example.android.popularmovies.pojo.MoviesResponse;
import com.example.android.popularmovies.utilites.MovieApiCallInterface;
import com.example.android.popularmovies.utilites.MovieApiModule;
import com.example.android.popularmovies.utilites.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NUM_LIST_ITEMS = 20;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String KEY_SERIAL_IMAGE = "image_serial";
    private final String KEY_CURRENT_SELECTION = "current_selection";
    public static final String MOVIE_ITEM_DATA_BUNDLE = "movie-item-data-bundle";

    private MovieAdapter mAdapter;
    private RecyclerView mImageList;
    public static final int OPTION_POPULAR = 0;
    private final String POPULAR = "popular";
    public static final int OPTION_TOP_RATED = 1;
    private final String TOP_RATED = "top_rated";
    public static final int OPTION_FAVORITES = 2;
    private boolean popular_set = false;
    private boolean top_rated_set = false;
    private boolean favorites_set = false;
    private static int currentSelection;
    private String FAVORITES_LIST_NAME = "favoritesList";
    private TextView errorMessageView;
    private ProgressBar progressBar;
    private boolean dataIsNull = true;
    private boolean networkCallMade = false;

    private List<MovieData> movieData, fMovieData;

    SharedPreferences favoritesPreference;
    Parcelable mImageRecyclerViewState;
    GridLayoutManager layoutManager;

    int mIndex = 0;

    private int sharedPreferenceIsEmpty;

    private static final int MOVIE_LOADER_ID = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        favoritesPreference =
                getApplicationContext().getSharedPreferences("favoritesPreference", 0);
       /* sharedPreferenceIsEmpty =
                favoritesPreference.getInt(FAVORITES_LIST_NAME + "_size", -1); */

        errorMessageView = findViewById(R.id.error_message_display);
        progressBar = findViewById(R.id.pb_loading_indicator);

        mImageList = findViewById(R.id.rv_images);

        if(savedInstanceState == null) {
            currentSelection = OPTION_POPULAR;
        } else {
            mIndex = savedInstanceState.getInt(KEY_SERIAL_IMAGE);
            currentSelection = savedInstanceState.getInt(KEY_CURRENT_SELECTION);
        }
        loadImageData(currentSelection);

        int numberOfColumns = 2;
        layoutManager = new GridLayoutManager(this, numberOfColumns);
        mImageList.setLayoutManager(layoutManager);
        mAdapter = new MovieAdapter(NUM_LIST_ITEMS);
        mAdapter.setmClickListener(this);
        mImageList.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
    }

    public static int getCurrentSelection() {
        return currentSelection;
    }

    public void setFavoritesActivityBackgroundColor(int color) {
        Log.d(TAG, "****************** setFavoritesActivityBackgroundColor");
        View view = this.getWindow().getDecorView();
        int favoritesNumber = favoritesPreference.getInt("favoritesNumber", 0);
        if (dataIsNull || favoritesNumber < 1) {
            view.setBackgroundColor(color);
        }
        else {
            view.setBackgroundResource(color);
        }
    }

    public void loadImageData(int selection) {

        if (selection == OPTION_POPULAR) {
            networkCall(POPULAR);
        }
        else if (selection == OPTION_TOP_RATED) {
            networkCall(TOP_RATED);
        }
        else if (selection == OPTION_FAVORITES) {
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        }
    }

    public void networkCall(String category) {
        Log.d(TAG, "****************** networkCall");
        errorMessageView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        mImageList.setVisibility(View.INVISIBLE);
        if (NetworkUtils.isNetworkAvailable(MainActivity.this)) {
            MovieApiCallInterface movieApiCallInterface =
                    MovieApiModule.getResponse().create(MovieApiCallInterface.class);
            Call<MoviesResponse> call =
                    movieApiCallInterface.getResponse(category, MovieApiModule.API_KEY_VALUE);
            networkCallMade = true;
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    Log.d(TAG, "****************** onResponse");
                    progressBar.setVisibility(View.INVISIBLE);
                    movieData = response.body().getResults();
                    mAdapter.setImageData(movieData);
                    showData();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d(TAG, "****************** onFailure");
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.e(TAG, t.toString());
                    Log.d(TAG, "****************** calling showErrorMessage from onFailure");
                    showErrorMessage();
                }
            });
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mMovieData = null;

            @Override
            protected void onStartLoading() {
                if (mMovieData != null) {
                    deliverResult(mMovieData);
                }
                else {
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable Cursor data) {
                mMovieData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor temp = data;
        if ((data != null) && (data.getCount() > 0)) {
            dataIsNull = false;
            data.moveToFirst();
            int length = data.getCount();
            fMovieData = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                MovieData favoriteMovie = new MovieData();
                favoriteMovie.setPosterPath(data.getString(
                        data.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMAGE_URL)));
                favoriteMovie.setTitle(data.getString(
                        data.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME)));
                favoriteMovie.setReleaseDate(data.getString(
                        data.getColumnIndex(MovieContract.MovieEntry.COLUMN_YEAR)));
                favoriteMovie.setVoteAverage(Float.parseFloat(data.getString(
                        data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING))));
                favoriteMovie.setOverview(data.getString(
                        data.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS)));
                favoriteMovie.setId(Integer.parseInt(data.getString(
                        data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID))));
                fMovieData.add(favoriteMovie);
                data.moveToNext();
            }
            mAdapter.swapCursor(temp);
            showData();
        }
        else {
            if (currentSelection == OPTION_FAVORITES) {
                Log.d(TAG, "****************** calling showErrorMessage from onLoadFinished");
                showErrorMessage();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);

    }

    /*private void storeArray(boolean[] favoritesList, String favorites_list_name) {
        if(sharedPreferenceIsEmpty < 0) {
            SharedPreferences.Editor editor = favoritesPreference.edit();
            editor.putInt(favorites_list_name + "_size", favoritesList.length);

            for (int i = 0; i < favoritesList.length; i++) {
                //storing sharedPreference values as key value pairs of movie_id and flag
                editor.putBoolean(String.valueOf(movieData.get(i).getId()), favoritesList[i]);
            }
            editor.apply();
        }
    }*/

    private void showData() {
        Log.d(TAG, "****************** showData");
        if (currentSelection == OPTION_FAVORITES) {
            setFavoritesActivityBackgroundColor(R.color.colorAccent);
            errorMessageView.setVisibility(View.INVISIBLE);
            mImageList.setVisibility(View.VISIBLE);
        }
        if(currentSelection != OPTION_FAVORITES) {
            errorMessageView.setVisibility(View.INVISIBLE);
            mImageList.setVisibility(View.VISIBLE);
            // Implementing the scroll to position so RecyclerView shows the same position on device rotation
            layoutManager.scrollToPosition(mIndex);
        }
    }

    private void showErrorMessage() {
        Log.d(TAG, "****************** showErrorMessage");
        if (currentSelection == OPTION_FAVORITES) {
            setFavoritesActivityBackgroundColor(Color.WHITE);
            errorMessageView.setText(R.string.error_message_favorites);
            errorMessageView.setVisibility(View.VISIBLE);
            mImageList.setVisibility(View.INVISIBLE);
        } else {
            errorMessageView.setText(R.string.error_message);
            if (networkCallMade) {
                errorMessageView.setVisibility(View.VISIBLE);
                mImageList.setVisibility(View.INVISIBLE);
            }
        }
        if(networkCallMade) {
            if (currentSelection == OPTION_POPULAR) {
                popular_set = false;
            } else if (currentSelection == OPTION_TOP_RATED) {
                top_rated_set = false;
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Context context = this;
        Class destinationClass = DetailsActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        Bundle bundle = new Bundle();

        if (currentSelection != OPTION_FAVORITES) {
            bundle.putParcelable(MOVIE_ITEM_DATA_BUNDLE, movieData.get(position));
            intentToStartDetailActivity.putExtras(bundle);
        }
        else {
            bundle.putParcelable(MOVIE_ITEM_DATA_BUNDLE, fMovieData.get(position));
            intentToStartDetailActivity.putExtras(bundle);
        }
        startActivity(intentToStartDetailActivity);
    }

    //TODO implement sidebar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.show_popular) {
            // If we are already showing the popular movies, do nothing
            if(currentSelection == OPTION_POPULAR && popular_set) {
                return true;
            }
            //Otherwise, change currentSelection and set the list to popular
            currentSelection = OPTION_POPULAR;
            loadImageData(currentSelection);
            popular_set = true;
            return true;
        }
        else if (id == R.id.show_top_rated) {
            //If we are already showing the top rated movies, do nothing
            if (currentSelection == OPTION_TOP_RATED && top_rated_set) {
                return true;
            }
            //Otherwise, change currentSelection and set the list to top rated
            currentSelection = OPTION_TOP_RATED;
            loadImageData(currentSelection);
            top_rated_set = true;
            return true;
        }
        else if (id == R.id.show_favorites) {
            //If we are already showing the favorite movies, do nothing
            if (currentSelection == OPTION_FAVORITES && favorites_set) {
                return true;
            }
            //Otherwise, change currentSelection and set the list to favorites
            currentSelection = OPTION_FAVORITES;
            loadImageData(currentSelection);
            favorites_set = true;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "****************** onSaveInstanceState");
        super.onSaveInstanceState(outState);

        mIndex = layoutManager.findFirstVisibleItemPosition();
        outState.putInt(KEY_SERIAL_IMAGE, mIndex);
        outState.putInt(KEY_CURRENT_SELECTION, currentSelection);
        mImageRecyclerViewState = layoutManager.onSaveInstanceState();
        outState.putParcelable(KEY_RECYCLER_STATE, mImageRecyclerViewState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "****************** onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mImageRecyclerViewState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "****************** onResume");
        super.onResume();
        if (mImageRecyclerViewState != null) {
            Log.d(TAG, "****************** mImageRecyclerViewState not null");
            layoutManager.onRestoreInstanceState(mImageRecyclerViewState);
        }
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }
}
