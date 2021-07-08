package com.example.icecreaminventory;

import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.icecreaminventory.data.IceCreamDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.icecreaminventory.data.IceCreamContract.IceCreamEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private IceCreamDbHelper mDbHelper;
    private static final int ICE_CREAM_LOADER=0;
    IceCreamCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView iceCreamListView=(ListView) findViewById(R.id.list);

        View emptyView=findViewById(R.id.empty_view);
        iceCreamListView.setEmptyView(emptyView);

        mCursorAdapter=new IceCreamCursorAdapter(this,null);
        iceCreamListView.setAdapter(mCursorAdapter);

        iceCreamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(IceCreamEntry.CONTENT_URI, id);
                intent.setData(currentPetUri);
                startActivity(intent);

            }
        });

        getLoaderManager().initLoader(ICE_CREAM_LOADER,null,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertIceCream();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertIceCream() {
        ContentValues values = new ContentValues();
        values.put(IceCreamEntry.COLUMN_NAME, "Vadilal Cornetto");
        values.put(IceCreamEntry.COLUMN_PRICE, 50.00);
        values.put(IceCreamEntry.COLUMN_QUANTITY, 10);
        values.put(IceCreamEntry.COLUMN_SUPPLIER_NAME, "Ice World");
        values.put(IceCreamEntry.COLUMN_SUPPLIER_PHONE, "+9999999999");
        values.put(IceCreamEntry.COLUMN_IMAGE, "android.resource://com.example.icecreaminventory/drawable/ic_cornetto");

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link StockEntry#CONTENT_URI} to indicate that we want to insert
        // into the stock database table.
        Uri newUri = getContentResolver().insert(IceCreamEntry.CONTENT_URI, values);
    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(IceCreamEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    public void clickOnSale(long id, int quantity) {
        ContentValues values = new ContentValues();
        values.put(IceCreamEntry._ID, id);
        values.put(IceCreamEntry.COLUMN_QUANTITY, quantity);
        String selection = IceCreamEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        if (quantity >= 0) {
            int rowsAffected = getContentResolver().update(IceCreamEntry.CONTENT_URI, values, selection, selectionArgs);
        }
        else{
            Toast.makeText(this, "Out of Stock", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                IceCreamEntry._ID,
                IceCreamEntry.COLUMN_NAME,
                IceCreamEntry.COLUMN_PRICE,
                IceCreamEntry.COLUMN_QUANTITY,
                IceCreamEntry.COLUMN_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                IceCreamEntry.CONTENT_URI,         // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}