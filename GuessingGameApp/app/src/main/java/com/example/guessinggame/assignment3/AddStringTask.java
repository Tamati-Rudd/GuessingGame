package com.example.guessinggame.assignment3;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//This activity handles user input for adding a String to be guessed
public class AddStringTask extends AsyncTask<String, Void, String> {
    private TextView strView;

    //Construct an AddStringTask
    public AddStringTask(TextView strView) {
        this.strView = strView;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL addStrUrl = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) addStrUrl.openConnection(); //Create HTTP connection to the web service
            connection.setReadTimeout(3000); //Set request parameters
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode(); //Get HTTP response code

            if (responseCode == HttpURLConnection.HTTP_OK)  { //Response is 200: Ok
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); //Create BufferedReader to read response

                StringBuilder textResponse = new StringBuilder(); //Use a StringBuilder to build a String representation of the JSON response
                String currentLine = "";
                while ((currentLine = in.readLine()) != null) { //Build the response String line by line
                    textResponse.append(currentLine);
                }
                in.close();
                return textResponse.toString();
            }
            else
                return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Once the worker thread (task) is completed, give the user feedback
    protected void onPostExecute(String result) { strView.setText(result); }
}
