package com.example.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    SQLiteManager sqLiteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_screen);

        sqLiteManager = SQLiteManager.instanceOfDatabase(this);
    }

    //Check that log-in credentials exist in DB table, and proceed to grid screen
    public void LogInAttempt(View view){

        //Get edit text field values
        EditText userNameEditText = (EditText) findViewById(R.id.editTextUName);
        String uName = userNameEditText.getText().toString();

        EditText pWordEditText = (EditText) findViewById(R.id.editTextPWord);
        String pWord = pWordEditText.getText().toString();

        if(sqLiteManager.DoesUserExist(uName, pWord)){
            //If match found, change to inventory screen
            Intent intent = new Intent(getApplicationContext(), GridActivity.class);
            startActivity(intent);
        } else {
            //No user exists... Display an alert dialog box
            ExampleDialog exampleDialog = new ExampleDialog("Invalid Log-In", "Please enter valid log-in credentials...");
            exampleDialog.show(getSupportFragmentManager(), "Dialog box");
        }
    }

    //If not already taken, create new log-in credentials in DB table
    public void CreateNewUser(View view){

        //Get edit text field values
        EditText userNameEditText = (EditText) findViewById(R.id.editTextUName);
        String uName = userNameEditText.getText().toString();

        EditText pWordEditText = (EditText) findViewById(R.id.editTextPWord);
        String pWord = pWordEditText.getText().toString();

        //If user does not exist already, go ahead and add to database
        if(!sqLiteManager.DoesUserExist(uName, pWord)){

            //Create user with given username and password
            sqLiteManager.AddUserToDB(uName, pWord);

            //Validate it was added, and change to inventory screen
            if(sqLiteManager.DoesUserExist(uName, pWord)) {
                Intent intent = new Intent(getApplicationContext(), GridActivity.class);
                startActivity(intent);
            }

        } else {
            //User already exists... Display alert dialog box
            ExampleDialog exampleDialog = new ExampleDialog("User already exists", "Please use log-in button...");
            exampleDialog.show(getSupportFragmentManager(), "Dialog box");
        }
    }
}