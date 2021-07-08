package com.example.icecreaminventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class IceCreamDbHelper  extends SQLiteOpenHelper {

    public static final String LOG_TAG = IceCreamDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "icecream.db";
    private static final int DATABASE_VERSION = 1;

    public IceCreamDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(IceCreamContract.IceCreamEntry.CREATE_TABLE_ICE_CREAM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
