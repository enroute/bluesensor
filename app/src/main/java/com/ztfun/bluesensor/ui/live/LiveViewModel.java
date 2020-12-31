package com.ztfun.bluesensor.ui.live;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ztfun.bluesensor.ui.OscilloscopeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveViewModel extends ViewModel {

    private Map<String, MutableLiveData<List<BluetoothDevice>>> btDevices;
    private Map<String, MutableLiveData<OscilloscopeView.DataSet>> liveData;

    public static final String[] BT_DEVICE_CATEGORY = new String[]
            {"AVAILABLE", "BOUND", "CONNECTED"};

    public static final String[] DATA_SET_LABEL = new String[]
            {"CURR", "VOLT", "TEMP"};

    // private MutableLiveData<Map<String, List<BluetoothDevice>>> btDevice;

    public LiveViewModel() {
        btDevices = new HashMap<>();
        for (String name : BT_DEVICE_CATEGORY) {
            MutableLiveData<List<BluetoothDevice>> data = new MutableLiveData<>();
            data.setValue(new ArrayList<BluetoothDevice>());
            btDevices.put(name, data);
        }

        liveData = new HashMap<>();
        for (String label : DATA_SET_LABEL) {
            MutableLiveData<OscilloscopeView.DataSet> data = new MutableLiveData<>();
            data.setValue(new OscilloscopeView.DataSet(label));
            liveData.put(label, data);
        }
    }

    public void addLiveDataEntry(String label, float x, float y) {
        try {
            liveData.get(label).getValue().addEntry(x, y);
        } catch (NullPointerException e) {
            // ignore
        }
    }
}