package com.example.guessinggame.assignment4;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.example.guessinggame.R;

//This activity sets up Bluetooth, ensuring a device is Bluetooth enabled & discoverable before trying to connect to another device
public class BluetoothActivity extends AppCompatActivity implements OnClickListener {
    private String username = "", chosenWord = "";
    private TextView bluetoothLog;
    private Button hostButton, joinButton;
    private BroadcastReceiver bluetoothReceiver;
    private String optionChosen = "";

    //on creation, set activity fields
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        chosenWord = intent.getStringExtra("chosen word");
        bluetoothLog = (TextView) findViewById(R.id.bluetoothLog);
        hostButton = (Button) findViewById(R.id.host_button);
        joinButton = (Button) findViewById(R.id.join_button);
    }

    //on start, setup Bluetooth & broadcast receiver
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onStart() {
        super.onStart();
        //Check device supports Bluetooth
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) { // Device doesn't support Bluetooth
            bluetoothLog.setText("Bluetooth not supported by device");
            hostButton.setEnabled(false);
            joinButton.setEnabled(false);
        } else {
            //Setup BroadcastReceiver to handle Bluetooth state changes & scan mode changes
            if (bluetoothReceiver == null) {
                bluetoothReceiver = new BluetoothReceiver();
                IntentFilter intentFilter = new IntentFilter(); //Filter the actions that the receiver is to handle
                intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(bluetoothReceiver, intentFilter);
            }

            //If Bluetooth supported, but not enabled, ask user to enable it
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothLog.setText("Bluetooth Disabled");
                hostButton.setEnabled(false);
                joinButton.setEnabled(false);
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBluetoothIntent);
            }
            else {
                bluetoothLog.setText("Bluetooth Enabled & Ready");
            }
        }
    }

    //Button click handling: record which button was clicked and ask permission to be discoverable
    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View view) {
        //Determine which button was pressed & record choice
        if (view == hostButton)  //Clicked to start as server
            optionChosen = "host";
        else if (view == joinButton) //Clicked to start as client
            optionChosen = "join";

        //Ask to make device discoverable (for 10 minutes)
        //Server or client will only be started if this is accepted
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    //Depending on option chosen, start as either server (host) or client (join)
    //Only runs once the user has agreed to make the device discoverable
    public void startServerOrClient() {
        if (optionChosen == "host") { //Start as server
            bluetoothLog.setText("Hosting a match");
            VersusNode versusNode = new VersusServer();
            Intent intent = new Intent(this, VersusActivity.class);
            intent.putExtra(VersusNode.class.getName(), versusNode);
            intent.putExtra("username", username);
            intent.putExtra("chosen word", chosenWord);
            startActivityForResult(intent, 1); //1 = request code
        }
        else if (optionChosen == "join") { //Start as client
            bluetoothLog.setText("Searching for match to join");
            VersusNode versusNode = new VersusClient();
            Intent intent = new Intent(this, VersusActivity.class);
            intent.putExtra(VersusNode.class.getName(), versusNode);
            intent.putExtra("username", username);
            intent.putExtra("chosen word", chosenWord);
            startActivityForResult(intent, 1); //1 = request code
        }
    }

    //When the back button in the action bar is clicked, set the username as result to return to parent activity
    @Override
    public boolean onSupportNavigateUp() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("username", username);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        return true;
    }

    //Get result from child activity to restore username data
    //Note: order is onCreate() > onStart() > onActivityResult() > onResume()
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1 && resultCode == RESULT_OK) { //Check response was ok
            if (intent != null) { //Get username back & run a GetDataTask
                username = intent.getStringExtra("username");
            }
        }
    }

    //Inner class that handles Bluetooth state & scan mode changes
    public class BluetoothReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) { //Received state change
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1); //The new state
                switch (state) {
                    case BluetoothAdapter.STATE_OFF: //Bluetooth has been disabled
                        bluetoothLog.setText("Bluetooth Disabled");
                        hostButton.setEnabled(false);
                        joinButton.setEnabled(false);
                        Log.w("BluetoothReceiver", "Bluetooth turned off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF: //Bluetooth is being disabled
                        bluetoothLog.setText("Bluetooth being Disabled");
                        hostButton.setEnabled(false);
                        joinButton.setEnabled(false);
                        Log.w("BluetoothReceiver", "Bluetooth turning off");
                        break;
                    case BluetoothAdapter.STATE_ON: //Bluetooth has been enabled
                        bluetoothLog.setText("Bluetooth Enabled & Ready");
                        hostButton.setEnabled(true);
                        joinButton.setEnabled(true);
                        Log.w("BluetoothReceiver", "Bluetooth turned on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON: //Bluetooth is being enabled
                        bluetoothLog.setText("Bluetooth being Enabled");
                        Log.w("BluetoothReceiver", "Bluetooth turning on");
                        break;
                }
            }
            else if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) { //Received scan mode change
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
                switch (scanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE: //Device is connectable & discoverable
                        Log.w("BluetoothReceiver", "Bluetooth is discoverable");
                        bluetoothLog.setText("Device is Discoverable");
                        startServerOrClient(); //Since device is now discoverable, can start server or client
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE: //Device is connectable but not discoverable
                        Log.w("BluetoothReceiver", "Bluetooth is connectable but not discoverable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE: //Device is not connectable or discoverable
                        Log.w("BluetoothReceiver", "Bluetooth not connectable or discoverable");
                        break;
                    default:
                        Log.w("BluetoothReceiver", "Received unknown scan mode");
                }
            }
        }
    }
}