package com.ztfun.bluesensor.ui;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedViewModel extends ViewModel {
    private static final String TAG = SharedViewModel.class.getSimpleName();

    private Map<String, MutableLiveData<List<BluetoothDevice>>> btDevices;
    private Map<String, MutableLiveData<OscilloscopeView.DataSet>> liveData;

    public static final String[] BT_DEVICE_CATEGORY = new String[]
            {"AVAILABLE", "BOUND", "CONNECTED"};

    public static final int INTENT_DATA_CURR = 0;
    public static final int INTENT_DATA_VOLT = 1;
    public static final int INTENT_DATA_TEMP = 2;
    public static final String[] DATA_SET_LABEL = new String[]
            {"CURR(mA)", "VOLT(mv)", "TEMP(℃)"};

    public SharedViewModel() {
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

        Log.d(TAG, "new SharedViewModel");
    }

    public void addLiveDataEntry(int labelIndex, float x, float y) {
        addLiveDataEntry(DATA_SET_LABEL[labelIndex], x, y);
    }

    public void addLiveDataEntry(String label, float x, float y) {
        try {
            liveData.get(label).getValue().addEntry(x, y);
            // Log.d(TAG, String.format("Adding entry to %s (%f, %f)", label, x, y));
        } catch (NullPointerException e) {
            // ignore
        }
    }

    public MutableLiveData<OscilloscopeView.DataSet> getLiveDataSet(String label) {
        return liveData.get(label);
    }
}
