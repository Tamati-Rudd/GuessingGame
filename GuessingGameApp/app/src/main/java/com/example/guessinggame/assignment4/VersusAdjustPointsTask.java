package com.example.guessinggame.assignment4;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//This AsyncTask sends a request to update the database in response to a completed versus match
public class VersusAdjustPointsTask extends AsyncTask<String, Void, String>  {
    TextView connectionStatus;
    boolean gainingPoints; //Whether the user is gaining or losing points

    //Constructor
    public VersusAdjustPointsTask(TextView connectionStatus, boolean gainingPoints) {
        this.connectionStatus = connectionStatus;
        this.gainingPoints = gainingPoints;
    }

    //Send request & get response
    @Override
    protected String doInBackground(String... params) {
        try {
            URL getDataUrl = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) getDataUrl.openConnection();
            connection.setReadTimeout(3000); // 3000ms
            connection.setConnectTimeout(3000); // 3000ms
            connection.setRequestMethod("POST");
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

    //Update UI to reflect points gained or lost
    protected void onPostExecute(String result) {
        if (result.equals("updated")) {
            if (gainingPoints)
                connectionStatus.setText(connectionStatus.getText()+" (+100 points)");
            else
                connectionStatus.setText(connectionStatus.getText()+" (-100 points)");
        }
    }
}
