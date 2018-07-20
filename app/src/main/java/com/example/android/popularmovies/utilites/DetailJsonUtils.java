package com.example.android.popularmovies.utilites;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Khare on 01-Apr-18.
 */

public class DetailJsonUtils {
    private static final String TAG = MovieJsonUtils.class.getSimpleName();
    private static URL[] movieTrailerUrl = null;
    private static String[] movieReviewAuthor = null;
    private static String[] movieReviewContent = null;

    static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    static final String WATCH_QUERY = "v";

    public static void getTrailerDetailsFromJson(Context context, String JsonStr) throws JSONException{

        JSONObject trailerJSON = new JSONObject(JsonStr);

        final String ARRAY_RESULTS = "results";

        final String IMAGE_KEY = "key";

        JSONArray trailerArray = trailerJSON.getJSONArray(ARRAY_RESULTS);
        movieTrailerUrl = new URL[trailerArray.length()];

        for (int i = 0; i < trailerArray.length(); i++) {
            String key;

            JSONObject selectedTrailer = trailerArray.getJSONObject(i);
            if (selectedTrailer.has(IMAGE_KEY)) {
                key = selectedTrailer.getString(IMAGE_KEY);
                Uri builtUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                        .appendQueryParameter(WATCH_QUERY, key)
                        .build();
                URL url = null;
                try {
                    url = new URL(builtUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                movieTrailerUrl[i] = url;
            }
        }
    }

    public static void getReviewDetailsFromJson (Context context, String JsonStr) throws JSONException {

        JSONObject reviewJSON = new JSONObject(JsonStr);

        final String ARRAY_RESULTS = "results";

        final String AUTHOR_NAME = "author";
        final String CONTENT = "content";

        JSONArray reviewArray = reviewJSON.getJSONArray(ARRAY_RESULTS);

        movieReviewAuthor = new String[reviewArray.length()];
        movieReviewContent = new String[reviewArray.length()];

        for (int i = 0; i < reviewArray.length(); i++) {
            String author;
            String content;

            JSONObject selectedReview = reviewArray.getJSONObject(i);
            if(selectedReview.has(AUTHOR_NAME)) {
                author = selectedReview.getString(AUTHOR_NAME);
                movieReviewAuthor[i] = author;
                Log.d(TAG, "Storing author and content into array "+ author + "<><><><><><<>><>");
            }
            if (selectedReview.has(CONTENT)) {
                content = selectedReview.getString(CONTENT);
                movieReviewContent[i] = content;
            }
        }
    }

    public static URL[] getMovieTrailerUrl() {
        return movieTrailerUrl;
    }

    public static String[] getMovieReviewAuthor() {
        return movieReviewAuthor;
    }

    public static String[] getMovieReviewContent() {
        return movieReviewContent;
    }
}
