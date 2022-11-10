package com.example.guessinggame.assignment3;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//This AsyncTask makes a server request to guess a single character/letter
public class GuessCharTask  extends AsyncTask<String, Void, String> {
    private TextView lettersView, hiddenStringView, pointsPossibleView;
    private ArrayList<Character> knownCharacters;
    private char letter;

    //Constructor
    public GuessCharTask(TextView lettersView, TextView hiddenStringView, TextView pointsPossibleView, ArrayList<Character> knownCharacters) {
        this.lettersView = lettersView;
        this.hiddenStringView = hiddenStringView;
        this.pointsPossibleView = pointsPossibleView;
        this.knownCharacters = knownCharacters;
    }

    //Send POST request & handle response
    @Override
    protected String doInBackground(String... params) {
        try {
            letter = params[0].charAt(params[0].length()-1); //Get letter from end of the URL
            URL charGuessUrl = new URL(params[0]); //Make URL object
            HttpURLConnection connection = (HttpURLConnection) charGuessUrl.openConnection(); //Create HTTP connection to the web service
            connection.setReadTimeout(3000); //Set request parameters
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            //Build JSON object to send to server as a string
            JSONArray array = new JSONArray(knownCharacters);
            JSONObject jsonObj = new JSONObject()
                    .put("characters", array);
            String json = jsonObj.toString();
            //Add JSON to request body
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(json);
            out.close();

            int responseCode = connection.getResponseCode(); //Get HTTP response code
            if (responseCode == HttpURLConnection.HTTP_OK) { //Response is 200: Ok
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); //Create BufferedReader to read response

                StringBuilder jsonResponse = new StringBuilder(); //Use a StringBuilder to build a String representation of the JSON response
                String currentLine = "";
                while ((currentLine = in.readLine()) != null) { //Build the response String line by line
                    jsonResponse.append(currentLine);
                }
                in.close();
                return jsonResponse.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    //Run once task completed to update UI and state
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                //Update knownCharacters ArrayList
                JSONArray array = jsonObj.getJSONArray("characters");
                for (int i = 0; i < array.length(); i++) {
                    knownCharacters.set(i, array.getString(i).charAt(0));
                }

                //Update UI
                String knownString = jsonObj.getString("knownString");
                hiddenStringView.setText(knownString);
                lettersView.setText(lettersView.getText()+" "+letter);

                //Reduce Possible Points (one char guess costs 5)
                int points = Integer.parseInt(pointsPossibleView.getText().toString());
                if (points > 10 && points <= 15)
                    points = 10;
                else if (points > 10)
                    points -= 5;
                pointsPossibleView.setText(Integer.toString(points));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}