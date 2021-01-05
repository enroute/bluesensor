package com.ztfun.bluesensor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.ztfun.bluesensor.events.CharacteristicChangedEvent;
import com.ztfun.bluesensor.events.CharacteristicReadEvent;
import com.ztfun.bluesensor.model.JigProtocol;
import com.ztfun.bluesensor.ui.OscilloscopeView;
import com.ztfun.bluesensor.ui.SharedViewModel;
import com.ztfun.module.plot.ZtPlotView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class FullscreenLiveActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_DATA = "intent.extra.data.type";
    // corresponds to index of SharedViewModel.DATA_SET_LABEL


    private int dataType;
    private ZtPlotView.DataSet voltDataSet, currDataSet;

//    OscilloscopeView.DataSet dataSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_live);

        dataType = getIntent().getIntExtra(INTENT_EXTRA_DATA, 0);
//        dataSet = new OscilloscopeView.DataSet(SharedViewModel.DATA_SET_LABEL[dataType]);
//        oscilloscopeView.registerData(dataSet);

        voltDataSet = new ZtPlotView.DataSet("Volt(mv)");
        List<String> voltLabels = new ArrayList<>();
        for(String label : new String[] {"0", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20"}) {
            voltLabels.add(label);
        }
        voltDataSet.setYTickLabels(voltLabels);

        currDataSet = new ZtPlotView.DataSet("Curr(mA)");
        ZtPlotView.DataSetOption currOption = new ZtPlotView.DataSetOption();
        currOption.dataSetType = ZtPlotView.DATA_SET_SECONDARY;
        currOption.paintColor = Color.parseColor("#00FFFF");
        currDataSet.setOption(currOption);
        List<String> currLabels = new ArrayList<>();
        for(String label : new String[] {"0", "0.2", "0.4", "0.6", "0.8", "1.0", "1.2", "1.4", "1.6", "1.8", "2.0"}) {
            currLabels.add(label);
        }
        currDataSet.setYTickLabels(currLabels);

        ZtPlotView ztPlotView = findViewById(R.id.fullscreen_oscilloscope);
        ztPlotView.addDataSet(voltDataSet);
        ztPlotView.addDataSet(currDataSet);
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
//                    dataSet.addEntry(jigPackage.time / 100, jigPackage.curr);
                    break;
                case SharedViewModel.INTENT_DATA_VOLT:
//                    dataSet.addEntry(jigPackage.time / 100, jigPackage.volt);
                    break;
                case SharedViewModel.INTENT_DATA_TEMP:
//                    dataSet.addEntry(jigPackage.time / 100, jigPackage.temp);
                    break;
                default:
                    break;
            }
        }
    }
}