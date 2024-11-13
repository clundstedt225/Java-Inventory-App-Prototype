package com.example.inventoryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

public class SQLiteManager extends SQLiteOpenHelper {

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    private static SQLiteManager sqLiteManager;

    private static final String DATABASE_NAME = "InventoryAppDB";
    private static final int DATABASE_VERSION = 1;


    //Table 1 (users table) details
    private static final String TABLE1_NAME = "Users";
    private static final String USER_ID = "_id"; //INT PRIMARY KEY
    private static final String USERNAME_FIELD = "username"; //TEXT
    private static final String PASSWORD_FIELD = "password"; //TEXT


    //Table 2 (inventory table) details
    private static final String TABLE2_NAME = "Inventory";
    private static final String ITEM_ID = "_id"; //INT PRIMARY KEY
    private static final String ITEMNAME_FIELD = "item_name"; //TEXT
    private static final String QUANTITY_FIELD = "quantity"; //INT


    public SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Return our SQLiteManager
    public static SQLiteManager instanceOfDatabase(Context context) {

        //Init value if not created yet
        if(sqLiteManager == null) {
            sqLiteManager = new SQLiteManager(context);
        }

        //Return the instance of the sqLiteManager
        return sqLiteManager;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create users table
        StringBuilder sql;
        sql = new StringBuilder()
                .append("CREATE TABLE ")
                .append(TABLE1_NAME)
                .append("(")
                .append(USER_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(USERNAME_FIELD)
                .append(" TEXT, ")
                .append(PASSWORD_FIELD)
                .append(" TEXT)");

        sqLiteDatabase.execSQL(sql.toString());

        //Create inventory table
        StringBuilder sql2;
        sql2 = new StringBuilder()
                .append("CREATE TABLE ")
                .append(TABLE2_NAME)
                .append("(")
                .append(ITEM_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(ITEMNAME_FIELD)
                .append(" TEXT, ")
                .append(QUANTITY_FIELD)
                .append(" INT)");

        sqLiteDatabase.execSQL(sql2.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    //Return true or false if username and password are found to exist
    public boolean DoesUserExist(String uName, String pWord){
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE1_NAME + " WHERE username = ? AND password = ?";
        try(Cursor result = db.rawQuery(sql, new String[] { uName, pWord }) ) {
            if(result.getCount() != 0) {
                return true; //user exists
            }
        }

        //No matching users found
        return false;
    }

    public void AddUserToDB(String uName, String pWord){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USERNAME_FIELD, uName);
        values.put(PASSWORD_FIELD, pWord);

        db.insert(TABLE1_NAME, null, values);
    }

    public void AddItemToDB(String iName, int qty){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ITEMNAME_FIELD, iName);
        values.put(QUANTITY_FIELD, qty);

        db.insert(TABLE2_NAME, null, values);
    }

    public void DeleteItemFromDB(int id){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE2_NAME, ITEM_ID + " = ?",
                new String[] { Integer.toString(id) });
    }

    public void UpdateItemQuantity(int id, int qty){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(QUANTITY_FIELD, qty);

        int rowsUpdated = db.update(TABLE2_NAME, values, ITEM_ID + " = ?",
                new String[] { Integer.toString(id) });
    }

    private void SendSMSAlert(Context context){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, you can send SMS
            String phoneNumber = "5551234567"; // Replace with the recipient's phone number
            String message = "Low Inventory Warning!";

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }

    //Pass in table layout
    public void PopulateItemsTable(TableLayout tableLayout, Context context) {
        SQLiteDatabase db = getReadableDatabase();

        try(Cursor result = db.rawQuery("SELECT * FROM " + TABLE2_NAME, null) ) {
            if(result.getCount() != 0) {

                //Go through process of creating new row for each table record
                while(result.moveToNext()) {
                    // Create a new TableRow for each data entry
                    TableRow tableRow = new TableRow(context);
                    tableRow.setLayoutParams(new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

                    //Create TextViews for id and item name only (index 0 and 1)
                    for (int i = 0; i < 2; i++) {
                        TextView textView = new TextView(context);
                        textView.setLayoutParams(new TableRow.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        textView.setText(result.getString(i));
                        textView.setGravity(Gravity.CENTER);

                        // Add the TextView to the TableRow (creating new column)
                        tableRow.addView(textView);
                    }

                    //Add quantity edit text to row
                    EditText editText = new EditText(context);
                    editText.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    ));

                    int id = result.getInt(0); // Store the ID

                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            //Only push update if field is not empty
                            if(charSequence.toString().trim().length() == 0){
                                //Nothing in text field, assume quantity of 0
                                UpdateItemQuantity(id, 0);

                                //Send sms alert if enabled
                                SendSMSAlert(context);
                            } else {

                                int newQty = Integer.parseInt(charSequence.toString().trim());
                                UpdateItemQuantity(id, newQty);

                                //Send sms alert if within threshold
                                if(newQty < 2 && newQty != 0){
                                    //Send sms alert if enabled
                                    SendSMSAlert(context);
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });

                    //Set edit text value to quantity number from DB
                    editText.setText(result.getString(2));
                    tableRow.addView(editText);

                    //Add delete button to row
                    Button newButton = new Button(context);
                    newButton.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    ));

                    // Add an OnClickListener to the button
                    newButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Remove item from DB using id in this row
                            DeleteItemFromDB(id);

                            //Delete self from table layout
                            tableLayout.removeView(tableRow);
                        }
                    });

                    newButton.setText("Remove");
                    tableRow.addView(newButton);

                    // Add the finished TableRow to the TableLayout
                    tableLayout.addView(tableRow);
                }
            }
        }
    }
}
