package com.ztfun.bluesensor.events;

import android.bluetooth.BluetoothGattCharacteristic;

public class CharacteristicReadEvent {
    public final BluetoothGattCharacteristic characteristic;
    public CharacteristicReadEvent(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }
}
