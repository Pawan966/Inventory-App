package com.example.icecreaminventory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.icecreaminventory.data.IceCreamContract.IceCreamEntry;

public class IceCreamCursorAdapter extends CursorAdapter {

    private final CatalogActivity catalogActivity;
    public IceCreamCursorAdapter(CatalogActivity context, Cursor c) {
        super(context, c, 0);
        this.catalogActivity = context;
    }

    public IceCreamCursorAdapter(Context context, Cursor c, CatalogActivity catalogActivity) {
        super(context, c, 0 /* flags */);
        this.catalogActivity = catalogActivity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.ice_cream_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView imageView = (ImageView) view.findViewById(R.id.ice_cream_image);
        Button sellButton = (Button) view.findViewById(R.id.sell_button);

        // Find the columns of stock attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(IceCreamEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(IceCreamEntry.COLUMN_IMAGE);

        // Read the stock attributes from the Cursor for the current stock
        final int stockId = cursor.getInt(idColumnIndex);
        String stockUnitName = cursor.getString(nameColumnIndex);
        float stockPrice = cursor.getFloat(priceColumnIndex);
        final int stockQuantity = cursor.getInt(quantityColumnIndex);
        String stockImageUri = cursor.getString(imageColumnIndex);

        // Update the TextViews with the attributes for the current stock
        nameTextView.setText(stockUnitName);
        quantityTextView.setText(String.valueOf(stockQuantity));
        priceTextView.setText(String.valueOf(stockPrice));

        if (!TextUtils.equals(stockImageUri, catalogActivity.getString(R.string.no_image))) {
            imageView.setImageURI(Uri.parse(stockImageUri));
        } else {

            imageView.setImageURI(Uri.parse(catalogActivity.getString(R.string.no_image_url)));
        }

        sellButton.setOnClickListener(new View.OnClickListener() {

            int updatedQuantity = stockQuantity - 1;

            @Override
            public void onClick(View v) {
                catalogActivity.clickOnSale(stockId,
                        updatedQuantity);
            }
        });
    }
}
