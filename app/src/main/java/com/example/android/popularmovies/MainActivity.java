package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.example.android.popularmovies.utilites.MovieJsonUtils;
import com.example.android.popularmovies.utilites.NetworkUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NUM_LIST_ITEMS = 20;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String KEY_SERIAL_IMAGE = "image_serial";
    private final String KEY_CURRENT_SELECTION = "current_selection";

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
    private URL[] imageUrls;
    private URL[] fimageUrls;
    private String[] yearData;
    private String[] fyearData;
    private String[] titleData;
    private String[] ftitleData;
    private String[] ratingData;
    private String[] fratingData;
    private String[] plotData;
    private String[] fplotData;
    private String[] movieIdData;
    private String[] fmovieIdData;
    private boolean[] favoritesList;
    private String FAVORITES_LIST_NAME = "favoritesList";
    private TextView errorMessageView;
    private ProgressBar progressBar;
    private boolean dataIsNull = true;
    private boolean onPostExecuteCalled = false;
    private boolean noNetwork = false;

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

        favoritesPreference = getApplicationContext().getSharedPreferences("favoritesPreference", 0);
        sharedPreferenceIsEmpty = favoritesPreference.getInt(FAVORITES_LIST_NAME + "_size", -1);



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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mIndex = layoutManager.findFirstVisibleItemPosition();
        outState.putInt(KEY_SERIAL_IMAGE, mIndex);
        outState.putInt(KEY_CURRENT_SELECTION, currentSelection);
        mImageRecyclerViewState = layoutManager.onSaveInstanceState();
        outState.putParcelable(KEY_RECYCLER_STATE, mImageRecyclerViewState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {


        if(savedInstanceState != null) {
            mImageRecyclerViewState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
        }

        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mImageRecyclerViewState != null) {
            layoutManager.onRestoreInstanceState(mImageRecyclerViewState);
            //Initially implemented the code in showData() here, moved it there because of the loading issue
            //layoutManager.scrollToPosition(mIndex);
        }
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static int getCurrentSelection() {
        return currentSelection;
    }

    public void setFavoritesActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        if (dataIsNull) {
            view.setBackgroundColor(color);
        }
        else {
            view.setBackgroundResource(color);
        }
    }

    public void loadImageData(int selection) {

        if (selection == OPTION_POPULAR) {
            new FetchImageTask().execute(POPULAR);
        }
        else if (selection == OPTION_TOP_RATED) {
            new FetchImageTask().execute(TOP_RATED);
        }
        else if (selection == OPTION_FAVORITES) {
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
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
            fimageUrls = new URL[length];
            ftitleData = new String[length];
            fyearData = new String[length];
            fratingData = new String[length];
            fplotData = new String[length];
            fmovieIdData = new String[length];
            for (int i = 0; i < data.getCount(); i++) {
                try {
                    fimageUrls[i] = new URL(data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMAGE_URL)));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                ftitleData[i] = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME));
                fyearData[i] = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_YEAR));
                fratingData[i] = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING));
                fplotData[i] = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS));
                fmovieIdData[i] = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                data.moveToNext();
            }
            mAdapter.swapCursor(temp);
            showData();
        }
        else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);

    }

    public class FetchImageTask extends AsyncTask<String, Void, URL[]> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            mImageList.setVisibility(View.INVISIBLE);
            errorMessageView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected URL[] doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            }
            String preference = strings[0];
            URL movieListRequestUrl = NetworkUtils.buildUrl(preference);
            try {
                String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieListRequestUrl);
                MovieJsonUtils
                        .getMovieDetailsFromJson(MainActivity.this, jsonMovieResponse);
                URL[] imageUrlData = MovieJsonUtils.getParsedImagePath();
                return imageUrlData;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(URL[] urls) {
            onPostExecuteCalled = true;
            progressBar.setVisibility(View.INVISIBLE);
            if(urls != null) {
                imageUrls = urls;
                titleData = MovieJsonUtils.getMovieTitle();
                yearData = MovieJsonUtils.getMovieYear();
                ratingData = MovieJsonUtils.getMovieRating();
                plotData = MovieJsonUtils.getMoviePlotDetails();
                movieIdData = MovieJsonUtils.getMovieId();
                favoritesList = new boolean[titleData.length];
                storeArray(favoritesList, FAVORITES_LIST_NAME);
                mAdapter.setImageData(urls);
                showData();
            }
            else {
                noNetwork = true;
                showErrorMessage();
            }
        }
    }

    private void storeArray(boolean[] favoritesList, String favorites_list_name) {
        if(sharedPreferenceIsEmpty < 0) {
            SharedPreferences.Editor editor = favoritesPreference.edit();
            editor.putInt(favorites_list_name + "_size", favoritesList.length);

            for (int i = 0; i < favoritesList.length; i++) {
                //storing sharedPreference values as key value pairs of movie_id and flag
                editor.putBoolean(movieIdData[i], favoritesList[i]);
            }
            editor.apply();
        }
    }

    private void showData() {
        if (currentSelection == OPTION_FAVORITES) {
            setFavoritesActivityBackgroundColor(R.color.colorAccent);
            errorMessageView.setVisibility(View.INVISIBLE);
            mImageList.setVisibility(View.VISIBLE);
        }
        if(onPostExecuteCalled && !(noNetwork) && (currentSelection != OPTION_FAVORITES)) {
            errorMessageView.setVisibility(View.INVISIBLE);
            mImageList.setVisibility(View.VISIBLE);
            // Implementing the scroll to position so RecyclerView shows the same position on device rotation
            layoutManager.scrollToPosition(mIndex);
        }
    }

    private void showErrorMessage() {
        if (currentSelection == OPTION_FAVORITES) {
            setFavoritesActivityBackgroundColor(Color.WHITE);
        }
        if (onPostExecuteCalled) {
            errorMessageView.setVisibility(View.VISIBLE);
            mImageList.setVisibility(View.INVISIBLE);
            if (currentSelection == OPTION_FAVORITES) {
                errorMessageView.setText(R.string.error_message_favorites);
            }
            else {
                errorMessageView.setText(R.string.error_message);
            }
        }
        if (currentSelection == OPTION_POPULAR) {
            popular_set = false;
        }
        else {
            top_rated_set = false;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Context context = this;
        Class destinationClass = DetailsActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);

        if (currentSelection != OPTION_FAVORITES) {
            // Pass the image URL to the DetailsActivity
            intentToStartDetailActivity.putExtra("imageURL", imageUrls[position].toString());
            intentToStartDetailActivity.putExtra("movieTitle", titleData[position]);
            intentToStartDetailActivity.putExtra("movieYear", yearData[position]);
            intentToStartDetailActivity.putExtra("movieRating", ratingData[position]);
            intentToStartDetailActivity.putExtra("moviePlot", plotData[position]);
            intentToStartDetailActivity.putExtra("movieId", movieIdData[position]);
        }
        else {
            intentToStartDetailActivity.putExtra("imageURL", fimageUrls[position].toString());
            intentToStartDetailActivity.putExtra("movieTitle", ftitleData[position]);
            intentToStartDetailActivity.putExtra("movieYear", fyearData[position]);
            intentToStartDetailActivity.putExtra("movieRating", fratingData[position]);
            intentToStartDetailActivity.putExtra("moviePlot", fplotData[position]);
            intentToStartDetailActivity.putExtra("movieId", fmovieIdData[position]);
        }
        startActivity(intentToStartDetailActivity);
    }

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
}
