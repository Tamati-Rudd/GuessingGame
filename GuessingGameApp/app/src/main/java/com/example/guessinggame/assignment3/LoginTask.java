package com.example.guessinggame.assignment3;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//This class logs a user into the Guessing Game Application (through a server request)
public class LoginTask extends AsyncTask<String, Void, String> {
    public static final String EXTRA_MESSAGE = "";
    Context context;

    //Construct a LoginTask
    public LoginTask(Context context) {
        this.context = context;
    }

    //Run the server communication as a separate thread
    @Override
    protected String doInBackground(String... params) {
        try {
            URL loginUrl = new URL(params[0]); //Make URL object
            HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection(); //Create HTTP connection to the web service
            connection.setReadTimeout(3000); //Set request parameters
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode(); //Get HTTP response code

            if (responseCode == HttpURLConnection.HTTP_OK)  { //Response is 200: Ok
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
            Log.e("Guessing Game", "Malformed URL: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Guessing Game", "IO Exception: " + e);
            e.printStackTrace();
        }
        return null;
    }

    //Once the worker thread (task) is completed, go to the NavigationActivity
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String username = jsonObj.getString("username");
                Intent intent = new Intent(context, NavigationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Add Flag to allow starting of an activity from outside an activity
                intent.putExtra(EXTRA_MESSAGE, username);
                context.startActivity(intent);
            } catch (JSONException e) {
                Log.e("Guessing Game", "JSON Exception: " + e);
                e.printStackTrace();
            }

        }
    }
}
