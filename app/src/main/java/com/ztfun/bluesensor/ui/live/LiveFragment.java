package com.ztfun.bluesensor.ui.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.ztfun.bluesensor.FullscreenLiveActivity;
import com.ztfun.bluesensor.MainActivity;
import com.ztfun.bluesensor.R;
import com.ztfun.bluesensor.events.CharacteristicChangedEvent;
import com.ztfun.bluesensor.events.CharacteristicReadEvent;
import com.ztfun.bluesensor.model.JigProtocol;
import com.ztfun.bluesensor.ui.SharedViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;

import static com.ztfun.Util.byte2Hex;

public class LiveFragment extends Fragment {

    private LiveViewModel liveViewModel;
    private SharedViewModel sharedViewModel;

    /* private OscilloscopeView.DataSet currData, voltData, tempData;*/

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        liveViewModel =
                ViewModelProviders.of(this).get(LiveViewModel.class);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        final ListView mLiveDataListView = root.findViewById(R.id.live_list);
        final LiveDataAdapter mLiveDataAdapter = new LiveDataAdapter(this.getContext(), R.layout.live_data_item_chart);

        /**
        currData = new OscilloscopeView.DataSet("Curr");
        voltData = new OscilloscopeView.DataSet("Volt");
        tempData = new OscilloscopeView.DataSet("Temp");
        mLiveDataAdapter.addDataSeries(currData);
        mLiveDataAdapter.addDataSeries(voltData);
        mLiveDataAdapter.addDataSeries(tempData);
         **/
        for (String label : SharedViewModel.DATA_SET_LABEL) {
            mLiveDataAdapter.addDataSeries(sharedViewModel.getLiveDataSet(label).getValue());
        }
        mLiveDataListView.setAdapter(mLiveDataAdapter);

        mLiveDataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //((MainActivity)requireActivity()).navigateTo(R.id.navigation_fullscreen);
                Intent intent = new Intent(requireActivity(), FullscreenLiveActivity.class);
                intent.putExtra(FullscreenLiveActivity.INTENT_EXTRA_DATA, position);
//                EditText editText = (EditText) findViewById(R.id.editText);
//                String message = editText.getText().toString();
//                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
//
//                startActivity(new Intent());
            }
        });

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

    private void handleReadValue(byte[] data) {
        JigProtocol.JigPackage jigPackage = JigProtocol.parse(data);
        if (jigPackage != null) {
            sharedViewModel.addLiveDataEntry(SharedViewModel.INTENT_DATA_VOLT, jigPackage.time / 100, jigPackage.volt);
            sharedViewModel.addLiveDataEntry(SharedViewModel.INTENT_DATA_CURR, jigPackage.time / 100, jigPackage.curr);
            sharedViewModel.addLiveDataEntry(SharedViewModel.INTENT_DATA_TEMP, jigPackage.time / 100, jigPackage.temp);
        }
    }

//    Thread dataWorker;
//    private static int workerX = 0;
//    private void startDataWorker() {
//        dataWorker = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int x = workerX;
//                try {
//                    float y;
//                    float scale = 200;
//                    float t;
//                    Random random = new Random();
//                    while (true) {
//                        Thread.sleep(10);
//
//                        t = (float) (x * Math.PI / scale);
//                        y = 15 * (1 + (float) Math.sin(t));
//                        sharedViewModel.addLiveDataEntry("CURR", x, y);
//                        // currData.addEntry(x, y);
//
//                        y = 5 * (1 + (float) Math.cos(t));
//                        //voltData.addEntry(x, y);
//                        // sharedViewModel.getLiveDataSet("VOLT").getValue().addEntry(x, y);
//                        sharedViewModel.addLiveDataEntry("VOLT", x, y);
//
//                        y = 5 * (1 + (float) (Math.sin(t) * Math.cos(t)));
//                        //tempData.addEntry(x, y);
//                        // sharedViewModel.getLiveDataSet("TEMP").getValue().addEntry(x, y);
//                        sharedViewModel.addLiveDataEntry("TEMP", x, y);
//
//                        x++;
//                        workerX = x;
//                    }
//                } catch (InterruptedException e) {
//
//                }
//                // Toast.makeText(getContext(), "worker stopped", Toast.LENGTH_LONG).show();
//            }
//        });
//        dataWorker.start();
//        //Toast.makeText(getContext(), "worker started", Toast.LENGTH_LONG).show();
//    }
//
//    private void stopDataWorker() {
//        if (dataWorker != null)
//            dataWorker.interrupt();
//        //Toast.makeText(getContext(), "worker stopped.", Toast.LENGTH_LONG).show();
//    }

    @Override
    public void onResume() {
        super.onResume();
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