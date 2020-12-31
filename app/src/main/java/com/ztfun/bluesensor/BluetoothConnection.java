package com.ztfun.bluesensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnection extends Thread{
    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    byte[] buffer;
    private Context context;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private int state;
    private Handler handler;
    private BluetoothDevice device;

    private static final String TAG = BluetoothConnection.class.getSimpleName();

    public static final int STATE_IDLE       = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED  = 2;
    public static final int STATE_CONNECTION_LOST = 3;

    private static final UUID BT_UUID = UUID.fromString("ddaa7f9e-c87e-44dc-aef1-ae53909b27b9");

    public BluetoothConnection(Handler handler, String address) {
        device = bluetoothAdapter.getRemoteDevice(address);
        setup(handler);
    }

    public BluetoothConnection(Handler handler, BluetoothDevice device) {
        this.device = device;
        setup(handler);
    }

    private void setup(Handler handler) {
        this.state = STATE_IDLE;
        this.handler = handler;
        BluetoothSocket btSocket = null;
        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        try {
            btSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = btSocket;

        // Now make the socket connection in separate thread to avoid FC
        Thread connectionThread = new ConnectionThread();
        connectionThread.start();
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = btSocket.getInputStream();
            tmpOut = btSocket.getOutputStream();
            buffer = new byte[1024];
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    private void startConnect(BluetoothDevice device) {

    }

    private byte[] read() {
        return null;
    }

    @Override
    public void run() {
        // wait for connected
        while (state == STATE_IDLE) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        Log.d(TAG, "connected.");

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read the data from socket stream
                mmInStream.read(buffer);
                // Send the obtained bytes to the UI Activity
            } catch (IOException e) {
                // An exception here marks connection loss
                // Send message to UI Activity
                Log.d(TAG, "read failed.");
                break;
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            // Write the data to socket stream
            mmOutStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void sendMessage(Message message) {
        // todo

    }

    class ConnectionThread extends Thread {
        @Override
        public void run() {
            super.run();
            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
                state = STATE_CONNECTED;
                // todo: change state and start connected thread
                Thread connectedThread = new ConnectedThread();
                connectedThread.start();
            } catch (IOException e) {
                // Connection to device failed, so close the socket
                Log.d(TAG, "connection to " + device.getName() + " failed.");
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    class ConnectedThread extends Thread {
        @Override
        public void run() {
            super.run();
            // todo:
        }
    }
}
