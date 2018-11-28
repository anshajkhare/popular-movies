package com.example.android.popularmovies.utilites;

import com.example.android.popularmovies.BuildConfig;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieApiModule {
    public static final String QUERY_BASE_URL = "https://api.themoviedb.org/3/movie/";
    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w500/";

    public static final String API_QUERY = "api_key";
    public static final String API_KEY_VALUE = BuildConfig.MoviesAPIKey;
    private static Retrofit retrofit = null;

    public static Retrofit getResponse() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(QUERY_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(
                            new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                    .build();
        }
        return retrofit;
    }
}
