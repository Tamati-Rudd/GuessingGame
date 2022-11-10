package com.example.guessinggame.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.guessinggame.R;

import java.util.ArrayList;

//This activity handles user input for Guessing a String
public class GuessActivity extends AppCompatActivity {
    private static final String EXTRA_MESSAGE = "";
    String username;
    ArrayList<Character> knownCharacters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);
        Intent intent = getIntent();
        username = intent.getStringExtra(LoginTask.EXTRA_MESSAGE);
        //Get initial hidden view of String
        knownCharacters = new ArrayList<>();
        TextView hiddenStringField = (TextView) findViewById(R.id.hiddenString);
        String url = getString(R.string.get_hidden_url);
        GetStringHiddenTask task = new GetStringHiddenTask(hiddenStringField, knownCharacters);
        task.execute(url);
    }

    //Guess Button Clicked
    public void guessSubmitted(View view) {
        EditText guessField = (EditText) findViewById(R.id.enterGuess);
        TextView guessFeedbackView = (TextView) findViewById(R.id.guessFeedbackView);
        TextView pointsAvailable = (TextView) findViewById(R.id.pointsAvailable);
        String guess = guessField.getText().toString();
        if (!TextUtils.isEmpty(guess) && guess.matches("^[a-zA-z ]*$")) { //Validate the user entered something
            if (guess.length() == 1 && guess != " ") { //Check whether String contains a single character
                String url = getString(R.string.user_url)+username+"/guess/"+guess;
                TextView lettersView = (TextView) findViewById(R.id.lettersGuessed);
                TextView hiddenStringView = (TextView) findViewById(R.id.hiddenString);
                GuessCharTask task = new GuessCharTask(lettersView, hiddenStringView, pointsAvailable, knownCharacters);
                task.execute(url);
            }
            else if (guess.length() >= 6 && guess.length() <= 12) { //Guess the entire String
                String url = getString(R.string.user_url)+username+"/guess/"+guess+"/"+pointsAvailable.getText().toString();
                Button guessButton = (Button) findViewById(R.id.submitGuessButton);
                GuessStringTask task = new GuessStringTask(guessFeedbackView, pointsAvailable, guessButton);
                task.execute(url);
            }
            else { //Invalid number of characters
                guessFeedbackView.setText("Must guess a single letter or the whole word");
            }
        } else {
            guessFeedbackView.setText("Please enter a guess (letters & spaces only)");
        }
    }

    //Go back to previous activity
    public void goBack(View view) {
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra(EXTRA_MESSAGE, username);
        startActivity(intent);
    }
}