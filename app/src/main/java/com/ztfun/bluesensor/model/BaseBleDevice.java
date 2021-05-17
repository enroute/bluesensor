package com.ztfun.bluesensor.model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;

public class BaseBleDevice {
    private static final String TAG = BaseBleDevice.class.getSimpleName();
    private Context context;
    private BluetoothGatt gatt;
    private String mac;
    private boolean serviceDiscovered;

    public String getMac() {
        return mac;
    }

    public BaseBleDevice(Context context, String address) {
        this.context = context;

        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            mac = address;
        } else {
            // todo: normalize mac

        }

        serviceDiscovered = false;
    }

    private boolean isConnected() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> deviceList = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device : deviceList) {
            if (device.getAddress().equals(mac)) {
                return true;
            }
        }

        return false;
    }

    public void connect() {
        if (gatt == null) {
            if (isConnected()) {
                // todo: something's wrong, some one else connected to the device,
                //  or our disconnect() method is not called before setting gatt to null.
            }

            gatt = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac).connectGatt(context, false, gattCallback);
        }
    }

    public void disconnect() {
        if (gatt != null) {
            gatt.disconnect();
        }
    }

    private void discoverServices() {
        if(! serviceDiscovered) {
            Log.d(TAG, "Discovering services by gatt.discoverServices()");

            // if gatt is null, stop trying
            if (gatt == null) {
                Log.d(TAG, "gatt is null, stop trying discover services.");
                return;
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // gatt might be set to null if user shutdown bluetooth
                    if (gatt != null) {
                        gatt.discoverServices();
                    }
                }
            });

            // try again in case of failure
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    discoverServices();
                }
            }, 500);
        } else {
            Log.d(TAG, "Abort service discovery since already discovered.");
        }
    }

    protected void setCharacteristics() {
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch (newState) {
                case BluetoothGatt.STATE_DISCONNECTED:
                    gatt.close();
                    BaseBleDevice.this.gatt = null;
                    serviceDiscovered = false;
                    break;

                case BluetoothGatt.STATE_CONNECTING:
                    break;

                case BluetoothGatt.STATE_CONNECTED:
                    discoverServices();
                    break;

                case BluetoothGatt.STATE_DISCONNECTING:
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            serviceDiscovered = true;
            setCharacteristics();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
}
