package com.example.AndroidExampleApp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.AndroidExampleApp.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ExampleActivity extends Activity {

    private String name="World";
    public static final UUID MY_UUID = new UUID(12345, 67890);
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_BT_WAIT = 2;

    private AcceptThread accept;
    private ConnectThread connect;
    private ConnectedThread connected;

    private BluetoothAdapter mBluetoothAdapter;
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            debugToast("onReceive");
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                debugToast("actionFound");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                debugToast(device.getName());
                connect = new ConnectThread(device);
                connect.run();
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(savedInstanceState != null){
            name = (String) savedInstanceState.get("name");
            TextView hello = (TextView) findViewById(R.id.helloText);
            hello.setText("Hello "+name+", ExampleActivity");
        }

        final Button button = (Button) findViewById(R.id.buTimer);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int time = 5;
                beforeTimer(time);
                Handler handler = new Handler();
                handler.postDelayed(runnable, time*1000);
            }
        });

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        final Button buBluetooth = (Button) findViewById(R.id.buBluetooth);
        buBluetooth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get bluetooth adapter
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    bluetoothNotSupported();
                }

                //enable bluetooth
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                else{
                    mBluetoothAdapter.startDiscovery();
                    debugToast("startDiscovery");
                }
            }
        });


        final Button buBluetoothWait = (Button) findViewById(R.id.buBluetoothWait);
        buBluetoothWait.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get bluetooth adapter
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    bluetoothNotSupported();
                }

                //enable bluetooth
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_WAIT);
                }
                else{
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                    accept = new AcceptThread();
                    accept.run();
                }
            }
        });

        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
    }

    private void bluetoothNotSupported(){
        Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
    }

    public void debugToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);

        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart(){
        super.onStart();

        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume(){
        super.onResume();

        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause(){
        super.onPause();

        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop(){
        super.onStop();

        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        EditText usernameEt = (EditText) findViewById(R.id.nameInput);
        String username = usernameEt.getText().toString();

        savedInstanceState.putString("name", username);
    }

    private void beforeTimer(int time){
        TextView hello = (TextView) findViewById(R.id.helloText);
        hello.setText("Timer started and set to "+time+" seconds");
        Toast.makeText(this, "Timer started", Toast.LENGTH_SHORT).show();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            afterTimer();
        }
    };

    public void afterTimer(){
        TextView hello = (TextView) findViewById(R.id.helloText);
        hello.setText("Timer finished");
        Toast.makeText(this, "Timer finished", Toast.LENGTH_SHORT).show();
    }

    private void showReceivedMessage(int message, byte[] buffer){
        String readMessage = new String(buffer, 0, message);
        TextView hello = (TextView) findViewById(R.id.helloText);
        hello.setText(readMessage);
        if(accept != null)
            accept.cancel();
        if(connect != null)
            connect.cancel();
        connected.cancel();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled
                    mBluetoothAdapter.startDiscovery();
                    debugToast("startDiscovery");
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, "Bluetooth not enabled",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ENABLE_BT_WAIT:
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                    accept = new AcceptThread();
                    accept.run();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, "Bluetooth not enabled",
                            Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Server", MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();

                    // If a connection was accepted
                    if (socket != null) {
                        // Do work to manage the connection (in a separate thread)
                        manageConnectedSocket(socket, false);
                        mmServerSocket.close();
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket, true);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket, boolean send){
        if(send){
            String stringMsg = "Bluetooth Test Nachricht";
            byte[] message = stringMsg.getBytes();
            connected = new ConnectedThread(socket);
            connected.write(message);
            if(accept != null)
                accept.cancel();
            if(connect != null)
                connect.cancel();
            connected.cancel();
        }else{
            connected = new ConnectedThread(socket);
            connected.run();
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    showReceivedMessage(bytes, buffer);
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
