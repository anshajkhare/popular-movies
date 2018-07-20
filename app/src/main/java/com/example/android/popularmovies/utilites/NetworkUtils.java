package com.example.android.popularmovies.utilites;

import android.net.Uri;

import com.example.android.popularmovies.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w500/";

    static final String API_QUERY_BASE_URL = "https://api.themoviedb.org/3/movie/";

    static final String API_QUERY = "api_key";
    static final String API_KEY_VALUE = BuildConfig.MoviesAPIKey;

    public static URL buildUrl(String sortBy) {
        Uri builtUri = Uri.parse(API_QUERY_BASE_URL).buildUpon()
                .appendPath(sortBy)
                .appendQueryParameter(API_QUERY, API_KEY_VALUE)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildTrailerListUrl(String movieId) {
        Uri builtUri = Uri.parse(API_QUERY_BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath("videos")
                .appendQueryParameter(API_QUERY, API_KEY_VALUE)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildReviewListUrl (String movieId) {
        Uri builtUri = Uri.parse(API_QUERY_BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath("reviews")
                .appendQueryParameter(API_QUERY, API_KEY_VALUE)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
