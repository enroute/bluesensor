package com.ztfun.bluesensor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FullscreenLiveActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_DATA = "intent.extra.data.type";
    // corresponds to index of SharedViewModel.DATA_SET_LABEL


//    private int dataType;
    private ZtPlotView.DataSet voltDataSet, currDataSet;
    private ZtPlotView.DataRange xRange;
    ZtPlotView ztPlotView;

//    OscilloscopeView.DataSet dataSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_live);

//        dataType = getIntent().getIntExtra(INTENT_EXTRA_DATA, 0);
//        dataSet = new OscilloscopeView.DataSet(SharedViewModel.DATA_SET_LABEL[dataType]);
//        oscilloscopeView.registerData(dataSet);

        voltDataSet = new ZtPlotView.DataSet("Volt(V)");
        List<String> voltLabels = new ArrayList<>();
        for(String label : new String[] {"0", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20"}) {
            voltLabels.add(label);
        }
        voltDataSet.setYTickLabels(voltLabels);
        voltDataSet.setDataRange(0, 20);

        currDataSet = new ZtPlotView.DataSet("Curr(A)");
        ZtPlotView.DataSetOption currOption = new ZtPlotView.DataSetOption();
        currOption.dataSetType = ZtPlotView.DATA_SET_SECONDARY;
        currOption.paintColor = Color.parseColor("#00FFFF");
        currDataSet.setOption(currOption);
        List<String> currLabels = new ArrayList<>();
        for(String label : new String[] {"0", "0.2", "0.4", "0.6", "0.8", "1.0", "1.2", "1.4", "1.6", "1.8", "2.0"}) {
            currLabels.add(label);
        }
        currDataSet.setYTickLabels(currLabels);
        currDataSet.setDataRange(0, 2.0);
//        currDataSet.addDataEntry(0, 0.2);
//        currDataSet.addDataEntry(60000, 0.4);
//        currDataSet.addDataEntry(120000, 0.8);
//        currDataSet.addDataEntry(600000, 1.5);


        ztPlotView = findViewById(R.id.fullscreen_oscilloscope);
        ztPlotView.addDataSet(voltDataSet);
        ztPlotView.addDataSet(currDataSet);
        // set fixed time labels
        List<String> timeLabels = new ArrayList<>(Arrays.asList(
                "5m", "4m30s", "4m", "3m30s", "3m", "2m30s", "2m", "1m30s", "1m", "30s", "0s"));
        ztPlotView.setXLabels(timeLabels);
        xRange = new ZtPlotView.DataRange(0.0, 600000.0);
        ztPlotView.setDataRangeX(xRange.min, xRange.max);  // milisec, 10min = 600 * 1000
    }

    Thread dataWorker;
    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        initDataFromDb();
    }

    private void initDataFromDb() {
        BlueSensorApplication application = ((BlueSensorApplication)getApplication());
        DbHelper dbHelper = application.getDbHelper();
        String address = application.getBleEngine().getCurrentRemoteAddress();
        if (address == null) {
            return;
        }
        List<JigProtocol.JigPackage> data = dbHelper.getDataByAddress(address);
        Log.d("DBVVV:", data == null ? "No data" : "GET DATA COUNT: " + data.size());

        // clear and then add to dataset
        currDataSet.dataEntries.clear();
        voltDataSet.dataEntries.clear();
        for (JigProtocol.JigPackage jigPackage : data) {
            ztPlotView.addData(currDataSet, jigPackage.curr / 1000.0, voltDataSet, jigPackage.volt / 1000.0);
        }
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

    private String formatMillisecond(double millisecond) {
        // to hh:mm:ss
        int secs = (int) (millisecond / 1000);
        int hh = secs / (60 * 60);
        int mm = (secs - hh * (60 * 60)) / 60;
        int ss = (secs - hh * (60 * 60) - mm * 60);
        return hh > 0 ? String.format(Locale.CHINA, "%02d:%02d:%02d", hh, mm ,ss) :
                String.format(Locale.CHINA, "%02d:%02d", mm ,ss);
    }

    private List<String> getTimeLabels() {
        List<String> timeLabels = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            timeLabels.add(formatMillisecond(xRange.min + i * 60000));
        }
        return timeLabels;
    }

    private void handleReadValue(byte[] data) {
        JigProtocol.JigPackage jigPackage = JigProtocol.parse(data);
        if (jigPackage != null) {
            ztPlotView.addData(currDataSet, jigPackage.curr / 1000.0, voltDataSet, jigPackage.volt / 1000.0);
//            switch(dataType) {
//                case SharedViewModel.INTENT_DATA_CURR:
//                    dataSet.addEntry(jigPackage.time / 100, jigPackage.curr);
//                    if (jigPackage.time > xRange.max) {
//                        // + 10 min
//                        int scale = (int)(Math.ceil((jigPackage.time - xRange.max) / 1000));
//                        xRange.min += 1000.0 * scale;
//                        xRange.max += 1000.0 * scale;
//                        ztPlotView.setDataRangeX(xRange.min, xRange.max);
//                        //ztPlotView.setXLabels(getTimeLabels());
//                    }
                    //currDataSet.addDataEntry(jigPackage.time, jigPackage.curr / 1000.0);
//                    break;
//                case SharedViewModel.INTENT_DATA_VOLT:
//                    dataSet.addEntry(jigPackage.time / 100, jigPackage.volt);
                    //voltDataSet.addDataEntry(jigPackage.time, jigPackage.volt / 1000.0);

//                    break;
//                case SharedViewModel.INTENT_DATA_TEMP:
////                    dataSet.addEntry(jigPackage.time / 100, jigPackage.temp);
//                    break;
//                default:
//                    break;
//            }
        }
    }
}