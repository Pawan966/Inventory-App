package com.example.icecreaminventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import com.example.icecreaminventory.data.IceCreamContract.IceCreamEntry;

public class IceCreamContract implements BaseColumns {

    public static final String CONTENT_AUTHORITY = "com.example.icecreaminventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ICE_CREAMS = "IceCreams";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private IceCreamContract() {}

    public static final class IceCreamEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ICE_CREAMS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ICE_CREAMS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ICE_CREAMS;

        public static final String TABLE_NAME = "IceCreams";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_PRICE = "price";

        public static final String COLUMN_QUANTITY = "quantity";

        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

        public static final String COLUMN_IMAGE = "image";

        public static final String CREATE_TABLE_ICE_CREAM = "CREATE TABLE " +
                IceCreamEntry.TABLE_NAME + "(" +
                IceCreamEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                IceCreamEntry.COLUMN_NAME + " TEXT NOT NULL," +
                IceCreamEntry.COLUMN_PRICE + " TEXT NOT NULL," +
                IceCreamEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                IceCreamEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL," +
                IceCreamEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL,"+
                IceCreamEntry.COLUMN_IMAGE + " TEXT NOT NULL"+ ");";
    }
}
