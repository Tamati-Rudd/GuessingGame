package com.example.guessinggame.assignment3;

import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//This AsyncTask gets user data from the server and displays it in an activity UI
public class GetUserDataTask extends AsyncTask<String, Void, String> {
    private TextView usernameView, pointsView, guessesView;

    //Construct a GetUserDataTask
    public GetUserDataTask(TextView usernameView, TextView pointsView, TextView guessesView) {
        this.usernameView = usernameView;
        this.pointsView = pointsView;
        this.guessesView = guessesView;
    }

    //Send GET Request
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

                StringBuilder jsonResponse = new StringBuilder(); //Use a StringBuilder to build a String representation of the JSON response
                String currentLine = "";
                while ((currentLine = in.readLine()) != null) { //Build the response String line by line
                    jsonResponse.append(currentLine);
                }
                in.close();
                return jsonResponse.toString();
            } else
                return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Parse JSON & Update UI
    protected void onPostExecute(String result) {
        try {
            JSONObject jsonObj = new JSONObject(result);
            String username = jsonObj.getString("username");
            String points = jsonObj.getString("points");
            String correctGuesses = jsonObj.getString("correctGuesses");
            usernameView.setText("Username: "+username);
            pointsView.setText("Total Points: "+points);
            guessesView.setText("Correct Guesses: "+correctGuesses);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
