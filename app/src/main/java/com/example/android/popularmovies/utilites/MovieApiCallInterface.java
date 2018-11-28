package com.example.android.popularmovies.utilites;

import com.example.android.popularmovies.pojo.MoviesResponse;
import com.example.android.popularmovies.pojo.ReviewResponse;
import com.example.android.popularmovies.pojo.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiCallInterface {
    @GET("{category}")
    Call<MoviesResponse> getResponse(@Path("category") String category, @Query(MovieApiModule.API_QUERY) String apiKey);

    @GET("{movieId}/videos")
    Call<TrailerResponse> getTrailers(@Path("movieId") String id, @Query(MovieApiModule.API_QUERY) String apiKey);

    @GET("{movieId}/reviews")
    Call<ReviewResponse> getReviews(@Path("movieId") String id, @Query(MovieApiModule.API_QUERY) String apiKey);
}

/*
// Declare all your API calls here
public interface MovieService {

    @GET("top_rated")
    Call<TopRatedMovies> getTopRatedMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int pageIndex
    );
}
 */