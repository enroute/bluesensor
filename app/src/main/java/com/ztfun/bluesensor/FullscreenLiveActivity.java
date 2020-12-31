package com.ztfun.bluesensor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ztfun.bluesensor.events.CharacteristicChangedEvent;
import com.ztfun.bluesensor.events.CharacteristicReadEvent;
import com.ztfun.bluesensor.model.JigProtocol;
import com.ztfun.bluesensor.ui.OscilloscopeView;
import com.ztfun.bluesensor.ui.SharedViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FullscreenLiveActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_DATA = "intent.extra.data.type";
    // corresponds to index of SharedViewModel.DATA_SET_LABEL


    private int dataType;

    OscilloscopeView.DataSet dataSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_live);
        // hide actionbar
        OscilloscopeView oscilloscopeView = findViewById(R.id.fullscreen_oscilloscope);

        dataType = getIntent().getIntExtra(INTENT_EXTRA_DATA, 0);
        dataSet = new OscilloscopeView.DataSet(SharedViewModel.DATA_SET_LABEL[dataType]);
        oscilloscopeView.registerData(dataSet);
    }

    Thread dataWorker;
    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CharacteristicChangedEvent event) {
        handleReadValue(event.characteristic.getValue());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CharacteristicReadEvent event) {
        handleReadValue(event.characteristic.getValue());
    }

    private void handleReadValue(byte[] data) {
        JigProtocol.JigPackage jigPackage = JigProtocol.parse(data);
        if (jigPackage != null) {
            switch(dataType) {
                case SharedViewModel.INTENT_DATA_CURR:
                    dataSet.addEntry(jigPackage.time / 100, jigPackage.curr);
                    break;
                case SharedViewModel.INTENT_DATA_VOLT:
                    dataSet.addEntry(jigPackage.time / 100, jigPackage.volt);
                    break;
                case SharedViewModel.INTENT_DATA_TEMP:
                    dataSet.addEntry(jigPackage.time / 100, jigPackage.temp);
                    break;
                default:
                    break;
            }
        }
    }
}