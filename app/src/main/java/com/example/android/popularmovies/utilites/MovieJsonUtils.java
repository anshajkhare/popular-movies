package com.example.android.popularmovies.utilites;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility functions to handle Image JSON data.
 */

public class MovieJsonUtils {

    private static final String TAG = MovieJsonUtils.class.getSimpleName();
    /* URL array to hold each movie's image URL */
    private static URL[] parsedImagePath = null;
    private static String[] movieTitle = null;
    private static String[] movieYear = null;
    private static String[] movieRating = null;
    private static String[] moviePlotDetails = null;
    private static String[] movieId = null;

    public static void getMovieDetailsFromJson(Context context, String movieJsonStr)
            throws JSONException {


        //URL[] parsedImagePath = null;

        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Movie information. Each movie's info is an element of the "result" array */
        final String ARRAY_RESULTS = "results";

        final String IMAGE_PATH = "poster_path";
        final String TITLE_PATH = "title";
        final String YEAR_PATH = "release_date";
        final String RATING_PATH = "vote_average";
        final String PLOT_PATH = "overview";
        final String MOVIE_ID_PATH = "id";

        final String MESSAGE_CODE = "status_code";

        /* Is there an error? */
        if (movieJson.has(MESSAGE_CODE)) {
            return;
        }

        JSONArray movieArray = movieJson.getJSONArray(ARRAY_RESULTS);

        parsedImagePath = new URL[movieArray.length()];
        movieTitle = new String[movieArray.length()];
        movieYear = new String[movieArray.length()];
        movieRating = new String[movieArray.length()];
        moviePlotDetails = new String[movieArray.length()];
        movieId = new String[movieArray.length()];

        for (int i = 0; i < movieArray.length(); i++) {
            String selectedImagePath;
            String selectedYear;
            String selectedRating;
            String id;

            /* Get the JSON object representing the movie */
            JSONObject selectedMovie = movieArray.getJSONObject(i);

            if (selectedMovie.has(TITLE_PATH)) {
                movieTitle[i] = selectedMovie.getString(TITLE_PATH);
            }
            selectedImagePath = selectedMovie.getString(IMAGE_PATH);
            if(selectedMovie.has(YEAR_PATH)) {
                selectedYear = selectedMovie.getString(YEAR_PATH).substring(0, 4);
                movieYear[i] = selectedYear;
            }
            if (selectedMovie.has(RATING_PATH)) {
                selectedRating = selectedMovie.getString(RATING_PATH) + "/10";
                movieRating[i] = selectedRating;
            }
            if(selectedMovie.has(PLOT_PATH)) {
                moviePlotDetails[i] = selectedMovie.getString(PLOT_PATH);
            }
            if(selectedMovie.has(MOVIE_ID_PATH)) {
                id = selectedMovie.getString(MOVIE_ID_PATH);
                movieId[i] = id;
            }

            Uri buildUri = Uri.parse(NetworkUtils.IMAGE_BASE_URL).buildUpon()
                    .appendEncodedPath(selectedImagePath)
                    .build();
            URL url = null;
            try {
                url = new URL(buildUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            parsedImagePath[i] = url;
        }
    }

    public static URL[] getParsedImagePath() {
        return parsedImagePath;
    }

    public static String[] getMovieTitle() {
        return movieTitle;
    }

    public static String[] getMoviePlotDetails() {
        return moviePlotDetails;
    }

    public static String[] getMovieRating() {
        return movieRating;
    }

    public static String[] getMovieYear() {
        return movieYear;
    }

    public static String[] getMovieId() {
        return movieId;
    }
}