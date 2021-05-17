package com.ztfun.bluesensor.model;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.BaseAdapter;

import com.ztfun.bluesensor.events.CharacteristicChangedEvent;
import com.ztfun.bluesensor.events.CharacteristicReadEvent;
import com.ztfun.bluesensor.events.ConnectionStateChangeEvent;
import com.ztfun.bluesensor.events.GattServiceDiscoveredEvent;
import com.ztfun.bluesensor.events.ReadRemoteRssiEvent;
import com.ztfun.bluesensor.events.ScanLeStopEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleEngine {
    private static final String TAG = BleEngine.class.getSimpleName();

    //public static final UUID BT_UUID = UUID.fromString("ddaa7f9e-c87e-44dc-aef1-ae53909b27b9");
    //public static final UUID BT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //public static final UUID BT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // GATT service UUID
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private Context context;
    private Handler handler;

    private boolean isScanning = false;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    // Stops scanning after 60 seconds.
    private static final long SCAN_PERIOD = 10000;

    public BleEngine(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.myLooper());

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    }

    public static void enableBle(Activity activity, final int REQUEST_ENABLE_BT) {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        BluetoothAdapter adapter = ((BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void scanLeDevice(final ScanCallback leScanCallback) {
        if (!isScanning) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    EventBus.getDefault().post(new ScanLeStopEvent());
                    Log.d(TAG, "scan stopped.");
                }
            }, SCAN_PERIOD);

            isScanning = true;
            Log.d(TAG, "ble scan started.");
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            isScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

//    public void connect(BluetoothDevice device) {
//        Log.d(TAG, "connect(" + device.getAddress() + ")");
//
//    }

    public boolean isLeScanning() {
        return isScanning;
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
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
            // post event
            EventBus.getDefault().post(new ConnectionStateChangeEvent(newState));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            EventBus.getDefault().post(new GattServiceDiscoveredEvent(status));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // post event
                EventBus.getDefault().post(new CharacteristicReadEvent(characteristic));
            }
            Log.d(TAG, "read" + characteristic.getValue().toString());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite: " + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            // post event
            EventBus.getDefault().post(new CharacteristicChangedEvent(characteristic));
            // Log.d(TAG, "characteristic changed:" + characteristic.getValue().toString());
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
            // post event
            EventBus.getDefault().post(new ReadRemoteRssiEvent(rssi));
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    public String getCurrentRemoteAddress() {
        return bluetoothDeviceAddress;
    }

    public String getCurrentRemoteName() {
        return bluetoothDeviceName;
    }

    private BluetoothGatt bluetoothGatt = null;

    private String bluetoothDeviceAddress = null;
    public String getBluetoothDeviceAddress() {
        return bluetoothDeviceAddress;
    }

    private String bluetoothDeviceName = null;
    public static final int CONNECTION_STATE_IDLE = 0;
    public static final int CONNECTION_STATE_CONNECTING = 1;
    public static final int CONNECTION_STATE_CONNECTED = 2;
    private int connectionState = CONNECTION_STATE_IDLE;
    public boolean connect(String address) {
        if (bluetoothAdapter == null || address == null) {
            return false;
        }

        // todo:
        // if previously connected device, try reconnect
        if (bluetoothGatt != null && bluetoothDeviceAddress != null && bluetoothDeviceAddress.equals(address)) {
            if (bluetoothGatt.connect()) {
                connectionState = CONNECTION_STATE_CONNECTING;
                return true;
            }
            return false;
        }

        // otherwise, start new connection
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            // device not found
            Log.d(TAG, "Device " + address + "not found. Unable to connect.");
            return false;
        }

        bluetoothDeviceName = device.getName();

        bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
        bluetoothDeviceAddress = address;
        connectionState = CONNECTION_STATE_CONNECTING;
        return true;
    }

    public void discoverServices() {
        bluetoothGatt.discoverServices();
    }

    public void setCharacteristicNotification() {
        // Iterate over GATT Services
        ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
        for (BluetoothGattService gattService : bluetoothGatt.getServices()) {
//            HashMap<String, String> currentServiceData =
//                    new HashMap<String, String>();
//            uuid = gattService.getUuid().toString();
//            currentServiceData.put(
//                    LIST_NAME, SampleGattAttributes.
//                            lookup(uuid, unknownServiceString));
//            currentServiceData.put(LIST_UUID, uuid);
//            gattServiceData.add(currentServiceData);
//
//            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
//                    new ArrayList<HashMap<String, String>>();

            // 获取服务中的特征集合
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();


            // 循环遍历特征集合
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                charas.add(gattCharacteristic);
//                HashMap<String, String> currentCharaData =
//                        new HashMap<String, String>();
//                uuid = gattCharacteristic.getUuid().toString();
//                currentCharaData.put(
//                        LIST_NAME, SampleGattAttributes.lookup(uuid,
//                                unknownCharaString));
//                currentCharaData.put(LIST_UUID, uuid);
//                gattCharacteristicGroupData.add(currentCharaData);
            }
//            mGattCharacteristics.add(charas);
//            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        for (BluetoothGattCharacteristic characteristic : charas) {
            setCharacteristicNotification(characteristic);
        }
    }

    BluetoothGattCharacteristic writeCharacteristic = null;
    BluetoothGattService writeService = null;
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt.setCharacteristicNotification(characteristic, true)) {
            Log.d(TAG, "Setting setCharacteristicNotification for characteristic: " + characteristic.getUuid());
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BT_UUID);
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            bluetoothGatt.writeDescriptor(descriptor);
            List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
            if(descriptorList != null && descriptorList.size() > 0) {
                for(BluetoothGattDescriptor descriptor : descriptorList) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                }
            }

            Log.d(TAG, "setCharacteristicNotification: " + characteristic.getUuid());
//            if (characteristic.getUuid().toString().equals(HEART_RATE_MEASUREMENT)) {
//                // save to write characteristic
//                writeCharacteristic = characteristic;
//                writeService = characteristic.getService();
//            }
        } else {
            Log.d(TAG, "fail to setCharacteristicNotification");
        }
    }

    public void write(byte[] data) {
        if (writeCharacteristic == null) {
            Log.d(TAG, "writeCharacteristic is null");
            return;
        }

        Log.d(TAG, "Writing " + JigProtocol.bytes2String(data));
        writeCharacteristic.setValue(data);
        boolean result = bluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    public List<BluetoothGattService> getGattServices() {
        return bluetoothGatt.getServices();
    }

    public void setWriteCharacteristic(BluetoothGattCharacteristic gattCharacteristic) {
        // todo: which characteristic is?
        // 00002a02-0000-1000-8000-00805f9b34fb
        // 00002a03-0000-1000-8000-00805f9b34fb
        // 0000fff1-0000-1000-8000-00805f9b34fb
        // 0000fff3-0000-1000-8000-00805f9b34fb
        // 0000fff5-0000-1000-8000-00805f9b34fb
        // 0000ffe1-0000-1000-8000-00805f9b34fb
        if (gattCharacteristic == null) {
            return;
        }

        String uuid = gattCharacteristic.getUuid().toString();
        if (uuid == null) {
            return;
        }

        if (uuid.equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
            writeCharacteristic = gattCharacteristic;
            Log.d(TAG, "Setting write chara to " + uuid.toString());
        }
        Log.d(TAG, "Writable characteristic: "  + gattCharacteristic.getUuid().toString());
    }
}
