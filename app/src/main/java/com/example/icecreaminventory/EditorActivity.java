package com.example.icecreaminventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.icecreaminventory.data.IceCreamContract.IceCreamEntry;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mNameEditText;
    private TextView mQuantity;
    private EditText mPrice;
    private EditText mSupplierName;
    private EditText mSupplierPhone;
    private ImageView mImage;
    private Button mSubtract;
    private Button mAdd;
    private Button mImageButton;
    private Button mCall;
    private Uri mCurrentIceCreamUri;
    private boolean mIceCreamHasChanged = false;
    private String mCurrentPhotoUri = "no images";
    private static final int EXISTING_ICE_CREAM_LOADER = 0;
    public static final int PICK_PHOTO_REQUEST = 20;
    /**
     * Permission Code for External Storage permission
     */
    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE = 21;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mIceCreamHasChanged=true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentIceCreamUri = intent.getData();
        if (mCurrentIceCreamUri==null){
            setTitle("Add an Ice Cream");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit an Ice Cream");
            getLoaderManager().initLoader(EXISTING_ICE_CREAM_LOADER, null,  this);
        }

        mNameEditText=(EditText) findViewById(R.id.stock_unit_name);
        mQuantity=(TextView) findViewById(R.id.quantity_text_view);
        mPrice=(EditText) findViewById(R.id.stock_unit_price);
        mSupplierName=(EditText) findViewById(R.id.supplier_name);
        mSupplierPhone=(EditText) findViewById(R.id.phone_number);
        mImage=(ImageView) findViewById(R.id.product_image_view);
        mSubtract=(Button)findViewById(R.id.subtract_quantity_button);
        mAdd=(Button)findViewById(R.id.add_quantity_button);
        mCall=(Button)findViewById(R.id.call);
        mImageButton=(Button)findViewById(R.id.add_image_button);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentValue = Integer.parseInt(mQuantity.getText().toString());
                int increasedValue = currentValue + 1;
                mQuantity.setText(String.valueOf(increasedValue));
            }
        });

        mSubtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentValue = Integer.parseInt(mQuantity.getText().toString());
                if (currentValue > 0) {
                    int decreasedValue = currentValue - 1;
                    mQuantity.setText(String.valueOf(decreasedValue));
                }
            }
        });

        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplierPhone = mSupplierPhone.getText().toString().trim();
                if(!TextUtils.isEmpty(supplierPhone)) {
                    Intent intent=new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+Uri.encode(supplierPhone)));
                    startActivity(intent);
                }
                else{
                    Toast.makeText(EditorActivity.this, "Phone Number not Available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getApplicationContext())
                      .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                      .withListener(new PermissionListener() {
                          @Override
                          public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                              Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                              photoPickerIntent.setType("image/*");
                              startActivityForResult(Intent.createChooser(photoPickerIntent,"Browse for image"),1);
                          }

                          @Override
                          public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                          }

                          @Override
                          public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                             permissionToken.continuePermissionRequest();
                          }
                      }).check();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK)
        {
            Uri mProductPhotoUri = data.getData();
            mCurrentPhotoUri = mProductPhotoUri.toString();
            mImage.setImageURI(Uri.parse(mCurrentPhotoUri));
        }
    }

    private void saveIceCream()
    {
        String name = mNameEditText.getText().toString().trim();
        int quantity = Integer.parseInt(mQuantity.getText().toString().trim());
        Float price=0.0f;
        String supplierName = mSupplierName.getText().toString().trim();
        String supplierPhone = mSupplierPhone.getText().toString().trim();

        if(!TextUtils.isEmpty(mPrice.getText().toString().trim()))
            price=Float.parseFloat(mPrice.getText().toString().trim());

        if (TextUtils.isEmpty(name)) {
            mNameEditText.setError("The Product Name cannot be blank");
            Toast.makeText(this, "The Product Name cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        } else if(TextUtils.isEmpty(mPrice.getText().toString().trim())){
            mPrice.setError("The Stock Unit Price Cannot be blank");
            Toast.makeText(this, "The Price Cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }else if(price<0.00){
            mPrice.setError("The Stock Unit Price Cannot be less than zero");
            Toast.makeText(this, "The Stock Unit Price Cannot be less than zero", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(supplierName)) {
            mSupplierName.setError("The Supplier Name cannot be blank");
            Toast.makeText(this, "The Supplier Name cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(supplierPhone)) {
            mSupplierPhone.setError("The Supplier Phone Number cannot be blank");
            Toast.makeText(this, "The Supplier Phone Number cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        } else{
            ContentValues values = new ContentValues();
            values.put(IceCreamEntry.COLUMN_NAME, name);
            values.put(IceCreamEntry.COLUMN_QUANTITY, quantity);
            values.put(IceCreamEntry.COLUMN_PRICE, price);
            values.put(IceCreamEntry.COLUMN_SUPPLIER_NAME, supplierName);
            values.put(IceCreamEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
            values.put(IceCreamEntry.COLUMN_IMAGE, mCurrentPhotoUri);

            if (mCurrentIceCreamUri == null) {
                // This is a NEW stock unit, so insert a new stock unit into the provider,
                // returning the content URI for the new stock unit.
                Uri newUri = getContentResolver().insert(IceCreamEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, "Error with saving Ice Cream",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, "Ice Cream Saved",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Otherwise this is an EXISTING stock unit, so update the stock unit with content URI: mCurrentStockUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentStockUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentIceCreamUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, "Error with updating Ice Cream",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, "Ice Cream Updated",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentIceCreamUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveIceCream();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if(!mIceCreamHasChanged){
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this Ice Cream");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteIceCream();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteIceCream() {
        if (mCurrentIceCreamUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentIceCreamUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error with Deleting Ice Cream",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ice Cream Deleted",
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mIceCreamHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                IceCreamEntry._ID,
                IceCreamEntry.COLUMN_NAME,
                IceCreamEntry.COLUMN_PRICE,
                IceCreamEntry.COLUMN_QUANTITY,
                IceCreamEntry.COLUMN_SUPPLIER_NAME,
                IceCreamEntry.COLUMN_SUPPLIER_PHONE,
                IceCreamEntry.COLUMN_IMAGE};

        return new CursorLoader(this,   // Parent activity context
                mCurrentIceCreamUri,         // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find the columns of stock unit attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_SUPPLIER_PHONE);
            int productImageColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String stock_name = cursor.getString(nameColumnIndex);
            float stock_price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier_name = cursor.getString(supplierNameColumnIndex);
            String supplier_phone = cursor.getString(supplierPhoneColumnIndex);
            String stock_image = cursor.getString(productImageColumnIndex);
            mCurrentPhotoUri = stock_image;

            // Update the views on the screen with the values from the database
            mNameEditText.setText(stock_name);
            mQuantity.setText(String.valueOf(quantity));
            mPrice.setText(String.valueOf(stock_price));
            mSupplierName.setText(supplier_name);
            mSupplierPhone.setText(supplier_phone);

            if (TextUtils.equals(stock_image, getString(R.string.no_image))) {
                mImage.setImageURI(Uri.parse(getString(R.string.no_image_url)));
            } else {
                mImage.setImageURI(Uri.parse(stock_image));
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantity.setText("");
        mPrice.setText("");
        mSupplierName.setText("");
        mSupplierPhone.setText("");
    }
}