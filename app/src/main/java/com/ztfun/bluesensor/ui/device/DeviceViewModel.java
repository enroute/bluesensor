package com.ztfun.bluesensor.ui.device;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class DeviceViewModel extends ViewModel {

    private SavedStateHandle savedStateHandle;

    public DeviceViewModel() {
        savedStateHandle = new SavedStateHandle();
        savedStateHandle.set("text", "Devices available");
    }

    public Object get(String key) {
        return savedStateHandle.get(key);
    }

    public void set(String key, Object value) {
        savedStateHandle.set(key, value);
    }
}