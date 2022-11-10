package com.example.guessinggame.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.guessinggame.R;

//First activity of the application, handling login
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //Called when the user clicks the login addStringButton
    public void login(View view) {
        //Get username & URL
        EditText usernameField = (EditText) findViewById(R.id.enterUsername);
        TextView loginView = (TextView) findViewById(R.id.loginView);
        if (!TextUtils.isEmpty(usernameField.getText().toString())) { //Validate the user entered something
            String username = usernameField.getText().toString() + "/";
            String url = getString(R.string.user_url);
            //Create and execute an Async task
            Context context = getApplicationContext();
            LoginTask task = new LoginTask(context);
            task.execute(url+username);
        } else {
            loginView.setText("Please enter your username");
        }
    }
}