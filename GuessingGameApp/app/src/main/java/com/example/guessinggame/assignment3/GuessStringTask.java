package com.example.guessinggame.assignment3;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//This AsyncTask is used to make a Guess to the server

public class GuessStringTask extends AsyncTask<String, Void, String> {
    private TextView guessFeedbackView, pointsPossibleView;
    private Button guessButton;

    //Constructor
    public GuessStringTask(TextView guessFeedbackView, TextView pointsPossibleView, Button guessButton) {
        this.guessFeedbackView = guessFeedbackView;
        this.pointsPossibleView = pointsPossibleView;
        this.guessButton = guessButton;
    }

    //Send Request
    @Override
    protected String doInBackground(String... params) {
        try {
            URL getDataUrl = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) getDataUrl.openConnection();
            connection.setReadTimeout(3000); // 3000ms
            connection.setConnectTimeout(3000); // 3000ms
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { //Response is 200: Ok
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); //Create BufferedReader to read response

                StringBuilder textResponse = new StringBuilder(); //Use a StringBuilder to build a String representation of the JSON response
                String currentLine = "";
                while ((currentLine = in.readLine()) != null) { //Build the response String line by line
                    textResponse.append(currentLine);
                }
                in.close();
                return textResponse.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    //Once Task Finished
    protected void onPostExecute(String result) {
        guessFeedbackView.setText(result);
        if (result.equals("Guess Incorrect")) { //If String guess was incorrect, deduct 10 points (minimum 10)
            int points = Integer.parseInt(pointsPossibleView.getText().toString());
            if (points > 10 && points <= 20)
                points = 10;
            else if (points > 10)
                points -= 10;
            pointsPossibleView.setText(Integer.toString(points));
        }
        else //Guess was correct, prevent further guesses
            guessButton.setEnabled(false);
    }
}
