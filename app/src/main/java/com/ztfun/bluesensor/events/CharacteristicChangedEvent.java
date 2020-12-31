package com.ztfun.bluesensor.events;

import android.bluetooth.BluetoothGattCharacteristic;

public class CharacteristicChangedEvent {
    public final BluetoothGattCharacteristic characteristic;
    public CharacteristicChangedEvent(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }
}
