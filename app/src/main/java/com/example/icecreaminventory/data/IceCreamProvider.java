package com.example.icecreaminventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.icecreaminventory.data.IceCreamContract.IceCreamEntry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.Provider;

public class IceCreamProvider extends ContentProvider {

    private static final int ICECREAMS = 100;

    private static final int ICE_CREAM_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(IceCreamContract.CONTENT_AUTHORITY,IceCreamContract.PATH_ICE_CREAMS ,ICECREAMS);
        sUriMatcher.addURI(IceCreamContract.CONTENT_AUTHORITY,IceCreamContract.PATH_ICE_CREAMS +"/#",ICE_CREAM_ID);
    }

    public static final String LOG_TAG = IceCreamProvider.class.getSimpleName();
    private IceCreamDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new IceCreamDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case ICECREAMS:
                // For the STOCK code, query the stock table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the stock table.
                cursor = database.query(IceCreamEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ICE_CREAM_ID:
                // For the STOCK_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.theleafapps.pro.stock/stock/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = IceCreamEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the stock table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(IceCreamEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ICECREAMS:
                return IceCreamEntry.CONTENT_LIST_TYPE;
            case ICE_CREAM_ID:
                return IceCreamEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ICECREAMS:
                return insertIceCream(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertIceCream(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(IceCreamEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Stock Unit requires a name");
        }

        float price = values.getAsFloat(IceCreamEntry.COLUMN_PRICE);
        if (price < 0) {
            throw new IllegalArgumentException("Stock Unit requires a price");
        }

        int quantity = values.getAsInteger(IceCreamEntry.COLUMN_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock Unit requires a quantity");
        }

        String supplierName = values.getAsString(IceCreamEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Supplier Name is required");
        }

        String supplierPhone = values.getAsString(IceCreamEntry.COLUMN_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Supplier Phone is required");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new stock entry with the given values
        long id = database.insert(IceCreamEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ICECREAMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(IceCreamEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ICE_CREAM_ID:
                // Delete a single row given by the ID in the URI
                selection = IceCreamEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(IceCreamEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ICECREAMS:
                return updateStock(uri, contentValues, selection, selectionArgs);
            case ICE_CREAM_ID:
                // For the STOCK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = IceCreamEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateStock(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateStock(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(IceCreamEntry.COLUMN_NAME)) {
            String name = values.getAsString(IceCreamEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Stock Unit requires a name");
            }
        }

        if (values.containsKey(IceCreamEntry.COLUMN_PRICE)) {
            float price = values.getAsFloat(IceCreamEntry.COLUMN_PRICE);
            if (price == 0) {
                throw new IllegalArgumentException("Stock Unit requires a price");
            }
        }

        if (values.containsKey(IceCreamEntry.COLUMN_QUANTITY)) {
            int quantity = values.getAsInteger(IceCreamEntry.COLUMN_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Stock Unit requires a quantity to be positive");
            }
        }

        if (values.containsKey(IceCreamEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(IceCreamEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Supplier Name is required");
            }
        }

        if (values.containsKey(IceCreamEntry.COLUMN_SUPPLIER_PHONE)) {
            String supplierPhone = values.getAsString(IceCreamEntry.COLUMN_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Supplier Phone is required");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(IceCreamEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }


}
