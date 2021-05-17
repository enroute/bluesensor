package com.ztfun.bluesensor.ui.history;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ztfun.bluesensor.DbHelper;
import com.ztfun.bluesensor.R;

import java.util.ArrayList;
import java.util.Set;

public class HistoryFragment extends Fragment {
    private static final String TAG = HistoryFragment.class.getSimpleName();

    private HistoryViewModel historyViewModel;
    private ListView boundDeviceListView;
    private ArrayAdapter boundDeviceListViewAdapter;
    private ArrayList<String> boundDeviceList = new ArrayList<String>();
    private BluetoothAdapter bluetoothAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel =
                ViewModelProviders.of(this).get(HistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        final TextView textView = root.findViewById(R.id.text_bound_device_title);
        historyViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boundDeviceList = new ArrayList<>();
        boundDeviceListViewAdapter = new ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1,
                boundDeviceList);
        boundDeviceListView = (ListView) root.findViewById(R.id.bound_device_list);
        boundDeviceListView.setAdapter(boundDeviceListViewAdapter);
        boundDeviceListView.setOnItemClickListener(itemClickListener);

        return root;
    }

    private void updatePairedDevices() {
        DbHelper dbHelper = new DbHelper(requireContext());
        for (DbHelper.Device device : dbHelper.getDevices()) {
            boundDeviceList.add(device.name + "\n" + device.address);
        }
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        boundDeviceList.clear();
//        for (BluetoothDevice device : pairedDevices) {
//            boundDeviceList.add(device.getName() + "\n" + device.getAddress());
//        }
        boundDeviceListViewAdapter.notifyDataSetChanged();
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(requireContext(), boundDeviceList.get(position), Toast.LENGTH_LONG).show();

            // start of test db
            DbHelper dbHelper = new DbHelper(requireContext());
            String[] lines = boundDeviceList.get(position).split("\\r?\\n");
            dbHelper.insertDevice(lines[1], lines[0], "");
            for (DbHelper.Device device : dbHelper.getDevices()) {
                Log.d(TAG, "Devices found in db-> " + device.toString());
            }
            // end of test db
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        updatePairedDevices();
    }
}