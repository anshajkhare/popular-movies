package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by Khare on 01-Apr-18.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";

    // If you change the database schema, you must increment the database version
    private static final int VERSION = 1;

    MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "  + MovieEntry.TABLE_NAME + " (" +
                //MovieEntry._ID +                " INTEGER PRIMARY KEY, " +
                //MovieEntry.COLUMN_MOVIE_ID +    " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_ID +    " TEXT PRIMARY KEY, " +
               // MovieEntry.COLUMN_FAVORITES +   " INTEGER DEFAULT 0," +
                MovieEntry.COLUMN_NAME +        " TEXT NOT NULL, " +
                MovieEntry.COLUMN_IMAGE_URL +   " TEXT NOT NULL, " +
                MovieEntry.COLUMN_YEAR +        " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING +      " TEXT NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS +    " TEXT NOT NULL);";
//                +
//                MovieEntry.COLUMN_TRAILER_REQUEST_URL_LIST + " TEXT NOT NULL, " +
//                MovieEntry.COLUMN_REVIEW_REQUEST_URL_LIST + " TEXT NOT NULL);";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
