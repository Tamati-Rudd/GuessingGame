package com.example.guessinggame.assignment3;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

//This task gets a fully hidden representation of the String to be guessed
public class GetStringHiddenTask extends AsyncTask<String, Void, String> {
    private TextView hiddenString;
    private ArrayList<Character> knownCharacters;

    public GetStringHiddenTask(TextView hiddenString, ArrayList<Character> knownCharacters) {
        this.hiddenString = hiddenString;
        this.knownCharacters = knownCharacters;
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
        hiddenString.setText(result);
        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '_')
                knownCharacters.add('_');
        }
    }
}
