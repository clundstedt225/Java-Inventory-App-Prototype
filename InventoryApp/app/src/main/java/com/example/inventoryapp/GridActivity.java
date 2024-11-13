package com.example.inventoryapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class GridActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    SQLiteManager sqLiteManager;
    TableLayout tLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_screen);

        //Set table layout ref
        tLayout = (TableLayout) findViewById(R.id.tableLayout);

        sqLiteManager = SQLiteManager.instanceOfDatabase(this);

        //Initial table population
        RefreshTable();
    }

    public void AddNewItem(View view){

        //Get edit text field values
        EditText itemNameEditText = (EditText) findViewById(R.id.editTextItemName);
        String iName = itemNameEditText.getText().toString();

        EditText qtyEditText = (EditText) findViewById(R.id.editTextQty);
        int qty = Integer.parseInt(qtyEditText.getText().toString());

        //Go back to table view
        setContentView(R.layout.grid_screen);

        //Add to DB and update table layout
        sqLiteManager.AddItemToDB(iName, qty);

        RefreshTable();
    }

    public void OpenNewItemScreen(View view){
        setContentView(R.layout.new_item_screen);
    }

    //Cancel button in newItemScreen
    public void OpenGridScreen(View view){
        setContentView(R.layout.grid_screen);

        //Table population
        RefreshTable();
    }

    //Clear table of old values
    private void RefreshTable(){
        //Re-establish table ref
        tLayout = (TableLayout) findViewById(R.id.tableLayout);

        // Start removing views from the second child (index 1)
        int childCount = tLayout.getChildCount();

        for (int i = childCount - 1; i > 0; i--) {
            View vw = tLayout.getChildAt(i);
            if (vw instanceof TableRow) {
                tLayout.removeViewAt(i);
            }
        }

        //Populate table
        sqLiteManager.PopulateItemsTable(tLayout, this);
    }

    public void ReqSMSPermissions(View view){
        // Check if SMS permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Request SMS permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions are already granted
        }
    }
}
