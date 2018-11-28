package com.example.android.popularmovies.pojo;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrailerResponse {

    @SerializedName("id")
    //@Expose
    private int id;
    @SerializedName("trailerData")
    @Expose
    private List<TrailerData> trailerData = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TrailerData> getTrailerData() {
        return trailerData;
    }

    public void setTrailerData(List<TrailerData> trailerData) {
        this.trailerData = trailerData;
    }

}