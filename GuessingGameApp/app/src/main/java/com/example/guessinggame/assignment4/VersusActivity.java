package com.example.guessinggame.assignment4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guessinggame.R;

import java.util.ArrayList;
import java.util.List;

//This activity runs the versus mode of the Guessing Game
public class VersusActivity extends AppCompatActivity implements View.OnClickListener {
    private String username = "", chosenWord = "", chosenWordHidden = "";
    private VersusNode versusNode;
    protected TextView connectionStatus, hiddenString;
    private EditText vsEnterGuess;
    protected Button ready, vsSubmitGuess;
    protected boolean connected = false;

    //On activity creation, setup activity fields
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        chosenWord = intent.getStringExtra("chosen word");
        createHiddenString(); //Create hidden representation of opponent's string to guess
        ready = (Button) findViewById(R.id.ready);
        versusNode = (VersusNode) intent.getExtras().get(VersusNode.class.getName());
        connectionStatus = (TextView) findViewById(R.id.connection_status);
        hiddenString = (TextView) findViewById(R.id.hidden_string);
        vsEnterGuess = (EditText) findViewById(R.id.vsEnterGuess);
        vsSubmitGuess = (Button) findViewById(R.id.vsSubmitGuess);
    }

    //On activity start, start the Versus Node thread
    @Override
    public void onStart() {
        super.onStart();
        versusNode.registerActivity(this);
        Thread thread = new Thread(versusNode);
        thread.start();
    }

    //When this activity stops, stop the versus node as well
    @Override
    public void onStop() {
        super.onStop();
        versusNode.stop();
        versusNode.registerActivity(null);
    }

    //Handle button clicks
    @Override
    public void onClick(View view) {
        if (view == ready) { //Ready button clicked
            String message = "ready";
            versusNode.forwardMessage(message);
            versusNode.forwardMessage(chosenWordHidden);
            ready.setEnabled(false);
            if (versusNode instanceof VersusServer) { //If this activity is the server, setup to take the first turn
                vsSubmitGuess.setEnabled(true);
                connectionStatus.setText("Game Started");
            }
        } else if (view == vsSubmitGuess) { //Guess button
            String guess = vsEnterGuess.getText().toString();

            if (!guess.isEmpty() && guess.matches("^[a-zA-z ]*$")) { //Validate guess input isn't empty & contains only letters & spaces
                if (guess.length() >= 6 && guess.length() <= 12) { //Guess the entire String
                    connectionStatus.setText("Guess Made: "+guess);
                    versusNode.forwardMessage(guess);
                    vsSubmitGuess.setEnabled(false);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Must guess a word or phrase with a length of 6-12 characters (including spaces)", Toast.LENGTH_SHORT);
                    toast.show();
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Guess must contain only letters & spaces", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    //Create a hidden representation of the opponent's word
    public void createHiddenString() {
        for (char c : chosenWord.toCharArray()) {
            if (c == ' ')
                chosenWordHidden += "  ";
            else
                chosenWordHidden += "_ ";
        }
    }

    //Process a String guess
    //Note: this is called by the player who DIDN'T make the guess - so if the guess is correct, the caller of this method LOSES the game!
    public void checkStringGuess(String guess) {
        if (guess.equalsIgnoreCase(chosenWord)) {
            connectionStatus.setText("Game Over: You LOST!");
            Toast toast = Toast.makeText(getApplicationContext(), "Your opponent guessed your word "+guess+". You lost :(", Toast.LENGTH_SHORT);
            toast.show();
            vsSubmitGuess.setEnabled(false);
            //Create AsyncTask to update loser's database record (-100 points)
            VersusAdjustPointsTask task = new VersusAdjustPointsTask(connectionStatus, false);
            String url = getString(R.string.user_url);
            task.execute(url+username+"/versus/-100");
            //send win message to the opponent
            versusNode.forwardMessage("win");
        } else { //Guess was incorrect, allow continued guessing
            vsSubmitGuess.setEnabled(true);
        }
    }

    //Declare winner and create AsyncTask to update winner's database record (+100 points)
    public void onWin() {
        connectionStatus.setText("Game Over: You WON!");
        Toast toast = Toast.makeText(getApplicationContext(), "You guessed the word correctly and WON THE GAME!", Toast.LENGTH_SHORT);
        toast.show();
        VersusAdjustPointsTask task = new VersusAdjustPointsTask(connectionStatus, true);
        String url = getString(R.string.user_url);
        task.execute(url+username+"/versus/100");
    }

    //When the back button in the action bar is clicked, stop the client/server & set the username as result to return to parent activity
    @Override
    public boolean onSupportNavigateUp() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("username", username);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        return true;
    }
}