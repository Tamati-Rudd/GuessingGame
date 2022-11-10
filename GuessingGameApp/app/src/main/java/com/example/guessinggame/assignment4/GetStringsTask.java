package com.example.guessinggame.assignment4;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//This async task gets the list of words from the server & places them into the recyclerView for display
public class GetStringsTask extends AsyncTask<String, Void, String> {
    private RecyclerView recyclerView;
    private ArrayList<String> words;

    //Constructor
    public GetStringsTask(RecyclerView recyclerView, ArrayList words) {
        this.recyclerView = recyclerView;
        this.words = words;
    }

    @Override
    protected String doInBackground(String... params) {
        URL getDataUrl = null;
        try {
            getDataUrl = new URL(params[0]);
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
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Parse JSON & populate RecyclerView
    protected void onPostExecute(String result) {
        try {
            JSONObject jsonObj = new JSONObject(result);
            JSONArray array = jsonObj.getJSONArray("strings");
            for (int i = 0; i < array.length(); i++) {
                words.add(array.getString(i));
            }

            //Update recyclerView
            recyclerView.getAdapter().notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
