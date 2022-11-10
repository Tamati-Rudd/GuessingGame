package com.example.guessinggame.assignment4;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

//This class contains the functionality of a Bluetooth client in the Versus service
public class VersusClient implements VersusNode {
    private VersusActivity versusActivity;
    private boolean stopRequested;
    private BluetoothSocket socket; //This client's connection socket (to the server)
    private BroadcastReceiver discoveryBroadcastReceiver; //Broadcast receiver for discovery state changes
    private List<BluetoothDevice> devices; //List of discovered Bluetooth devices
    private List<String> messages; //List of messages to be sent to the server

    //Constructor
    public VersusClient() {
        versusActivity = null;
        socket = null;
        discoveryBroadcastReceiver = null;
        devices = new ArrayList<>();
        messages = new ArrayList<>();
    }

    //Register the Versus Activity of this client
    @Override
    public void registerActivity(VersusActivity versusActivity) {
        this.versusActivity = versusActivity;
    }

    //Add a message to be sent (to the server) & notify waiting threads
    @Override
    public void forwardMessage(String message) {
        synchronized (messages) {
            messages.add(message);
            messages.notifyAll();
        }
    }

    //Stop this client
    @Override
    public void stop() {
        stopRequested = true;

        //Unregister & close Broadcast Receiver
        if (discoveryBroadcastReceiver != null) {
            versusActivity.unregisterReceiver(discoveryBroadcastReceiver);
            discoveryBroadcastReceiver = null;
        }

        //Close all device discovery & messages to be sent threads
        synchronized (devices) {
            devices.notifyAll();
        }
        synchronized (messages) {
            messages.notifyAll();
        }

        //Close the client socket (connection with the server)
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) { }
        }
    }

    //Run the client as a thread
    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        //Set initial client state
        stopRequested = false;
        devices.clear();
        messages.clear();

        //Setup Broadcast Receiver to handle discovery state updates
        discoveryBroadcastReceiver = new DiscoveryBroadcastReceiver();
        IntentFilter discoveryIntentFilter = new IntentFilter(); //Filter the actions that the receiver is to handle
        discoveryIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        versusActivity.registerReceiver(discoveryBroadcastReceiver, discoveryIntentFilter);

        //Start device discovery
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean started = bluetoothAdapter.startDiscovery();
        versusActivity.connectionStatus.setText("Discovery Started: "+started);



        //Thread waits until discovery is fully completed
        synchronized (devices) {
            try {
                devices.wait();
            } catch (InterruptedException e) {
            }
        }

        //If no devices found: stop this client
        if (devices.size() == 0 && !stopRequested) {
            versusActivity.connectionStatus.setText("No devices found");
            stopRequested = true;
            return;
        }

        //If at least one device found: check for server (Bluetooth service UUID)
        socket = null;
        for (BluetoothDevice device : devices) {
            try {
                versusActivity.connectionStatus.setText("Attempting connection");
                //Try to create a connection to a server device using the service UUID
                socket = device.createRfcommSocketToServiceRecord(VersusNode.SERVICE_UUID);
                //Found a server: open connection & end discovery
                socket.connect();
                bluetoothAdapter.cancelDiscovery();
                break;
            } catch (IOException e) { //Connection failed: try next device
                socket = null;
            }
        }

        //If no server connection found: stop this client
        if (socket == null) {
            versusActivity.connectionStatus.setText("No server found");
            stopRequested = true;
            return;
        }

        //If a server connection was found: setup MessageMailer thread
        MessageMailer messageMailer = new MessageMailer();
        Thread mailerThread = new Thread(messageMailer);
        mailerThread.start();
        versusActivity.connectionStatus.setText("Connected to Server");
        versusActivity.connected = true;
        versusActivity.runOnUiThread(new Runnable() { //Enable ready button for the client
            @Override
            public void run() {
                versusActivity.ready.setEnabled(true);
            }
        });

        //Listen for incoming messages from the server
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!stopRequested) { //Continuously check for server messages while running
                String message = reader.readLine();
                if (versusActivity != null) { //Handle incoming message
                    if (message.equals("ready")) {
                        versusActivity.connectionStatus.setText("Game Started");

                    } else if (message.contains("_")) { //Hidden string representation message
                        versusActivity.runOnUiThread(new Runnable() { //Perform button enable/disable on the UI thread
                            @Override
                            public void run() {
                                versusActivity.hiddenString.setText(message);
                            }
                        });
                    } else if (message.equals("win")) { //won the game
                        versusActivity.runOnUiThread(new Runnable() { //Perform button enable/disable on the UI thread
                            @Override
                            public void run() {
                                versusActivity.onWin();
                            }
                        });
                    } else { //String guess
                        versusActivity.runOnUiThread(new Runnable() { //Perform button enable/disable on the UI thread
                            @Override
                            public void run() {
                                versusActivity.checkStringGuess(message);
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally { //Close the connection socket to the server
            try {
                socket.close();
            } catch (IOException e) { }
            socket = null;
        }
    }

    //Inner class to define a broadcast receiver for receiving discovery state changes
    public class DiscoveryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { //If a device has been found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                synchronized (devices) {
                    devices.add(device);
                } //Note: newer API can use device.fetchUuidsWithSdp (instead of intent.getParcelableExtra)
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) { //If device discovery has been started
                versusActivity.connectionStatus.setText("Searching for devices");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { //If device discovery has finished
                synchronized (devices) { //Notify the main client thread that device discovery has finished
                    versusActivity.connectionStatus.setText("Device discovery finished");
                    devices.notifyAll();
                }
            }
        }
    }

    //Inner class that sends messages to the server
    private class MessageMailer implements Runnable {
        public void run() {
            //Setup print writer to send message
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            } catch (IOException e) { //Stop if cannot read messages from server
                e.printStackTrace();
                stop();
            }

            while (!stopRequested) {
                String message;
                synchronized (messages) { //GET A MESSAGE TO SEND
                    while (messages.size() == 0) { //If no message to send, wait until notification that a message has been received
                        try {
                            messages.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (stopRequested) //If the client is to shutdown, exit (return)
                            return;
                    }

                    //Once forwardMessage() notifies, this code will run
                    message = messages.remove(0); //Get first message from the ArrayList
                }

                //Send message to server
                writer.println(message);
                writer.flush();
            }
        }
    }
}
