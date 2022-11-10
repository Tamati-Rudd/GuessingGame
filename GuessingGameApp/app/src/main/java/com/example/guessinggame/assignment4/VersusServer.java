package com.example.guessinggame.assignment4;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

//This class contains the functionality of a Bluetooth server in the Versus service
public class VersusServer implements VersusNode {
    private VersusActivity versusActivity;
    private boolean stopRequested;
    private List<ClientConnection> clientConnections; //List of connections to clients
    private List<String> messages; //List of messages to be sent to client(s)

    //Construct a server instance
    public VersusServer() {
        versusActivity = null;
        clientConnections = new ArrayList<>();
        messages = new ArrayList<>();
    }

    //Register the Versus Activity of this server
    @Override
    public void registerActivity(VersusActivity versusActivity) {
        this.versusActivity = versusActivity;
    }

    //Add a message to be sent (to clients) & notify waiting threads
    @Override
    public void forwardMessage(String message) {
        synchronized (messages) {
            messages.add(message);
            messages.notifyAll();
        }
    }

    //Stop the server
    @Override
    public void stop() {
        stopRequested = true;
        synchronized (messages) {
            messages.notifyAll();
        }

        //Close all connections with clients
        for (ClientConnection connection : clientConnections) {
            connection.closeConnection();
        }
    }

    //Run the server as a thread
    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        //START THE BLUETOOTH SERVICE & SERVER
        //Set initial server state
        stopRequested = false;
        clientConnections.clear();
        messages.clear();
        BluetoothServerSocket serverSocket = null;

        try { //Start the Bluetooth service
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(VersusNode.SERVICE_NAME, VersusNode.SERVICE_UUID);
            versusActivity.connectionStatus.setText("Server Started. Listening for connections");
        } catch (IOException e) { //Error starting service
            versusActivity.connectionStatus.setText("Error starting server");
            Log.e("VersusServer", "Error starting service: " + e);
            e.printStackTrace();
            return;
        }

        //Prepare a MessageMailer thread to handle sending of messages
        MessageMailer mailer = new MessageMailer();
        Thread mailerThread = new Thread(mailer);
        mailerThread.start();

        //CONTINUOUSLY LISTEN FOR INCOMING CLIENT CONNECTIONS (until server stopped)
        while (!stopRequested) {
            try {
                BluetoothSocket socket = serverSocket.accept(500); //Accept incoming connection to the server socket & get reference to client socket
                ClientConnection connection = new ClientConnection(socket); //Create a new Client Connection to store connection with the new client
                clientConnections.add(connection); //Add the Client Connection to the list
                Thread clientThread = new Thread(connection); //Create a client thread to continuously read messages from that client (see ClientConnection run())
                clientThread.start(); //Start client thread
                versusActivity.connectionStatus.setText("Connected to Client");
                versusActivity.connected = true;
            } catch (IOException e) { }
        }

        //SERVER IS STOPPING: CLOSE SERVER SOCKET
        try {
            serverSocket.close();
        } catch (IOException e) { }
    }

    //Inner class to define a connection through which to handle incoming client communication
    private class ClientConnection implements Runnable {
        private BluetoothSocket socket;
        private PrintWriter writer;

        //Construct a client connection object
        public ClientConnection(BluetoothSocket socket) {
            this.socket = socket;
            try {
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Repeatedly listen for incoming messages to the server
        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (!stopRequested) { //Continuously attempt to read messages
                    String message = reader.readLine();
                    if (message.equals("ready")) { //Ready message
                        versusActivity.connectionStatus.setText("Client "+message);
                        versusActivity.runOnUiThread(new Runnable() { //Perform button enable/disable on the UI thread
                            @Override
                            public void run() {
                                versusActivity.ready.setEnabled(true);
                            }
                        });
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Send a message to a client
        public void send(String message) throws IOException {
            writer.println(message);
            writer.flush();
        }

        //Close a single connection (socket) with a client
        public void closeConnection() {
            try {
                socket.close();
            }
            catch (IOException e) {}

            clientConnections.remove(this); //Remove this object from the connections list
        }
    }

    //Inner class that sends messages to client versus node(s)
    private class MessageMailer implements Runnable {
        public void run() {
            while (!stopRequested) {
                String message;
                synchronized (messages) { //GET A MESSAGE TO SEND
                    while (messages.size() == 0) { //If no message to send, wait until notification that a message has been received
                        try {
                            messages.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (stopRequested) //If the server is to shutdown, exit (return)
                            return;
                    }

                    //Once forwardMessage() notifies, this code will run
                    message = messages.remove(0); //Get first message from the ArrayList
                }

                //SEND THE MESSAGE TO ALL CLIENTS
                for (ClientConnection connection : clientConnections) {
                    try {
                        connection.send(message);
                    } catch (IOException e) {
                        Log.e("VersusServer", "A message has been dropped: " + message);
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
