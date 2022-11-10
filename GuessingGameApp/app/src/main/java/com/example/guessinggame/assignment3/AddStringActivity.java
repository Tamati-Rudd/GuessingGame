package com.example.guessinggame.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.guessinggame.R;

public class AddStringActivity extends AppCompatActivity {
    String username;
    private static final String EXTRA_MESSAGE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_string);
        Intent intent = getIntent();
        username = intent.getStringExtra(LoginTask.EXTRA_MESSAGE);
        Intent i = new Intent();
        i.putExtra(EXTRA_MESSAGE, username);
        setResult(Activity.RESULT_OK, i);
    }

    //Create a task to send a request to the server to add a String
    public void addString(View view) {
        EditText strField = (EditText) findViewById(R.id.enterString);
        TextView strView = (TextView) findViewById(R.id.strView);
        String strToAdd = strField.getText().toString();

        //Validate String input, creating an Async task if valid
        if (!TextUtils.isEmpty(strToAdd) && strToAdd.length() >= 6 && strToAdd.length() <= 12){
            if (strToAdd.matches("^[a-zA-z ]*$")) {
                String str = strField.getText().toString() + "/";
                String url = getString(R.string.user_url);
                //Create and execute an Async task
                AddStringTask task = new AddStringTask(strView);
                task.execute(url + username + "/" + str);
            } else {
                strView.setText("The word(s) can only contain letters & spaces");
            }
        } else {
            strView.setText("Please enter a 6-12 letter word or phrase");
        }
    }

    //Go back to previous activity
    public void goBack(View view) {
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra(EXTRA_MESSAGE, username);
        startActivity(intent);
    }
}