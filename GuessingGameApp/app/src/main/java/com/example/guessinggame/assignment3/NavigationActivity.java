package com.example.guessinggame.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.guessinggame.R;
import com.example.guessinggame.assignment4.BluetoothActivity;
import com.example.guessinggame.assignment4.SelectWordActivity;

//This activity handles navigation to either the AddStringActivity or the GuessActivity
public class NavigationActivity extends AppCompatActivity {
    String username;
    private static final String EXTRA_MESSAGE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        //Get username and call AsyncTask to load user data
        Intent intent = getIntent();
        username = intent.getStringExtra(LoginTask.EXTRA_MESSAGE);
        if (username != null) {
            runGetDataTask();
        }
    }

    //Load User Data through an Async Task
    public void runGetDataTask() {
        String url = getString(R.string.user_url);
        TextView usernameView = (TextView) findViewById(R.id.usernameView);
        TextView pointsView = (TextView) findViewById(R.id.pointsView);
        TextView guessesView = (TextView) findViewById(R.id.guessesView);
        GetUserDataTask task = new GetUserDataTask(usernameView, pointsView, guessesView);
        usernameView.setText(url+username);
        task.execute(url+username);
    }

    //Handle the Add String button being pressed
    public void addStringPressed(View view) {
        Intent intent = new Intent(this, AddStringActivity.class);
        intent.putExtra(EXTRA_MESSAGE, username);
        startActivity(intent);
    }

    //Handle the Play button being pressed
    public void playGame(View view) {
        Intent intent = new Intent(this, GuessActivity.class);
        intent.putExtra(EXTRA_MESSAGE, username);
        startActivity(intent);
    }

    //Handle the Play Versus button being pressed
    public void playVersus(View view) {
        Intent intent = new Intent(this, SelectWordActivity.class);
        intent.putExtra("username", username);
        startActivityForResult(intent, 1); //1 = request code
    }

    //Get result from child activity to restore username data
    //Note: order is onCreate() > onStart() > onActivityResult() > onResume()
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1 && resultCode == RESULT_OK) { //Check response was ok
            if (intent != null) { //Get username back & run a GetDataTask
                username = intent.getStringExtra("username");
                runGetDataTask();
            }
        }
    }
}