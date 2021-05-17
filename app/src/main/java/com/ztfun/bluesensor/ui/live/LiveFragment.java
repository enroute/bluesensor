package com.ztfun.bluesensor.ui.live;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.ztfun.bluesensor.BlueSensorApplication;
import com.ztfun.bluesensor.DbHelper;
import com.ztfun.bluesensor.FullscreenLiveActivity;
import com.ztfun.bluesensor.MainActivity;
import com.ztfun.bluesensor.R;
import com.ztfun.bluesensor.events.CharacteristicChangedEvent;
import com.ztfun.bluesensor.events.CharacteristicReadEvent;
import com.ztfun.bluesensor.model.BleEngine;
import com.ztfun.bluesensor.model.JigProtocol;
import com.ztfun.bluesensor.ui.SharedViewModel;
import com.ztfun.module.plot.ZtPlotView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.ztfun.Util.byte2Hex;

public class LiveFragment extends Fragment {

    private LiveViewModel liveViewModel;
    private SharedViewModel sharedViewModel;

    private TextView tvMode, tvTemp, tvFreq, tvDeviceAddress, tvDeviceName;
    private BleEngine bleEngine;

    /* private OscilloscopeView.DataSet currData, voltData, tempData;*/

    private ZtPlotView.DataSet voltDataSet, currDataSet;
    private ZtPlotView.DataRange xRange;
    ZtPlotView ztPlotView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        liveViewModel =
                ViewModelProviders.of(this).get(LiveViewModel.class);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tvMode = root.findViewById(R.id.tv_mode);
        tvTemp = root.findViewById(R.id.tv_temp);
        tvFreq = root.findViewById(R.id.tv_freq);
        tvDeviceAddress = root.findViewById(R.id.tv_device_address);
        tvDeviceName = root.findViewById(R.id.tv_device_name);

        root.findViewById(R.id.bt_change_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChangeModeDialog();
            }
        });

        bleEngine = ((BlueSensorApplication)(requireActivity().getApplication())).getBleEngine();

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


        ztPlotView = root.findViewById(R.id.live_plot);
        ztPlotView.addDataSet(voltDataSet);
        ztPlotView.addDataSet(currDataSet);
        List<String> timeLabels = new ArrayList<>();
        for (String label : new String[] {
                "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
                "06:00", "07:00", "08:00", "09:00", "10:00"}) {
            timeLabels.add(label);
        }
        //ztPlotView.setXLabels(timeLabels);
        xRange = new ZtPlotView.DataRange(0.0, 600000.0);
        ztPlotView.setDataRangeX(xRange.min, xRange.max);  // milisec, 10min = 600 * 1000
        ztPlotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), FullscreenLiveActivity.class));
            }
        });

//        final ListView mLiveDataListView = root.findViewById(R.id.live_list);
//        final LiveDataAdapter mLiveDataAdapter = new LiveDataAdapter(this.getContext(), R.layout.live_data_item_chart);

        /**
        currData = new OscilloscopeView.DataSet("Curr");
        voltData = new OscilloscopeView.DataSet("Volt");
        tempData = new OscilloscopeView.DataSet("Temp");
        mLiveDataAdapter.addDataSeries(currData);
        mLiveDataAdapter.addDataSeries(voltData);
        mLiveDataAdapter.addDataSeries(tempData);
         **/
//        for (String label : SharedViewModel.DATA_SET_LABEL) {
//            mLiveDataAdapter.addDataSeries(sharedViewModel.getLiveDataSet(label).getValue());
//        }
//        mLiveDataListView.setAdapter(mLiveDataAdapter);
//
//        mLiveDataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //((MainActivity)requireActivity()).navigateTo(R.id.navigation_fullscreen);
//                Intent intent = new Intent(requireActivity(), FullscreenLiveActivity.class);
//                intent.putExtra(FullscreenLiveActivity.INTENT_EXTRA_DATA, position);
////                EditText editText = (EditText) findViewById(R.id.editText);
////                String message = editText.getText().toString();
////                intent.putExtra(EXTRA_MESSAGE, message);
//                startActivity(intent);
////
////                startActivity(new Intent());
//            }
//        });

        //for(int i = 0; i < mLiveDataAdapter.getCount(); i++) {
            //OscilloscopeView view = (OscilloscopeView) mLiveDataListView.getChildAt(0);
            //view.registerData((OscilloscopeView.DataSet)mLiveDataAdapter.getItem(0));
            //view.registerData(currData);
        //}

//        liveViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                // textView.setText(s);
//            }
//        });

        //startDataWorker();
        EventBus.getDefault().register(this);

        return root;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CharacteristicChangedEvent event) {
        handleReadValue(event.characteristic.getValue());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CharacteristicReadEvent event) {
        handleReadValue(event.characteristic.getValue());
    }

    private void startChangeModeDialog() {
        final int[] mode = new int[1];
        new AlertDialog.Builder(requireActivity()).setIcon(R.drawable.ble)
                .setTitle(R.string.change_mode)
                .setSingleChoiceItems(JigProtocol.MODE_LABEL, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mode[0] = which;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(requireActivity(), "Changing mode to " + JigProtocol.getModeLabel(mode[0]), Toast.LENGTH_LONG).show();
                        switch (mode[0]) {
                            case 0:
                                bleEngine.write(JigProtocol.getJigCommandBytes(JigProtocol.CMD_SWITCH_TO_5W));
                                break;
                            case 1:
                                bleEngine.write(JigProtocol.getJigCommandBytes(JigProtocol.CMD_SWITCH_TO_75W));
                                break;
                            case 2:
                                bleEngine.write(JigProtocol.getJigCommandBytes(JigProtocol.CMD_SWITCH_TO_10W));
                                break;
                            case 3:
                                bleEngine.write(JigProtocol.getJigCommandBytes(JigProtocol.CMD_SWITCH_TO_15W));
                                break;
                        }
                    }
                })
                .create()
                .show();
    }

    private void handleReadValue(byte[] data) {
        JigProtocol.JigPackage jigPackage = JigProtocol.parse(data);
        if (jigPackage != null) {
//            sharedViewModel.addLiveDataEntry(SharedViewModel.INTENT_DATA_VOLT, jigPackage.time / 100, jigPackage.volt);
//            sharedViewModel.addLiveDataEntry(SharedViewModel.INTENT_DATA_CURR, jigPackage.time / 100, jigPackage.curr);
//            sharedViewModel.addLiveDataEntry(SharedViewModel.INTENT_DATA_TEMP, jigPackage.time / 100, jigPackage.temp);

            tvMode.setText(JigProtocol.getModeLabel(jigPackage.mode));
            tvTemp.setText("" + jigPackage.temp);
            tvFreq.setText("" + jigPackage.freq);

//            if (jigPackage.time > xRange.max) {
//                // + 10 min
//                int scale = (int)(Math.ceil((jigPackage.time - xRange.max) / 1000));
//                xRange.min += 1000.0 * scale;
//                xRange.max += 1000.0 * scale;
//                ztPlotView.setDataRangeX(xRange.min, xRange.max);
//                //ztPlotView.setXLabels(getTimeLabels());
//            }

            //currDataSet.addDataEntry(jigPackage.time, jigPackage.curr / 1000.0);
            //voltDataSet.addDataEntry(jigPackage.time, jigPackage.volt / 1000.0);
            ztPlotView.addData(currDataSet, jigPackage.curr / 1000.0, voltDataSet, jigPackage.volt / 1000.0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        tvDeviceName.setText(bleEngine.getCurrentRemoteName());
        tvDeviceAddress.setText(bleEngine.getCurrentRemoteAddress());
        
        initDataFromDb();
    }

    private void initDataFromDb() {
        BlueSensorApplication application = ((BlueSensorApplication)getActivity().getApplication());
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // restore data
            Toast.makeText(getContext(), "restore data here.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        //stopDataWorker();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // save data
        Toast.makeText(getContext(), "Save data here.", Toast.LENGTH_SHORT).show();
        outState.putString("aaa", "teete");
    }
}