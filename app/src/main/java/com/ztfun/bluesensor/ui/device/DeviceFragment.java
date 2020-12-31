package com.ztfun.bluesensor.ui.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.ztfun.bluesensor.BaseEventBusFragment;
import com.ztfun.bluesensor.BlueSensorApplication;
import com.ztfun.bluesensor.BluetoothConnection;
import com.ztfun.bluesensor.R;
import com.ztfun.bluesensor.events.ScanLeStopEvent;
import com.ztfun.bluesensor.model.BleEngine;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends BaseEventBusFragment {
    private static final String TAG = DeviceFragment.class.getSimpleName();

    private DeviceViewModel deviceViewModel;
    private ListView deviceListView;
//    private ArrayAdapter deviceListViewAdapter;
//    private ArrayList<String> deviceList = new ArrayList<String>();
    private ScanDeviceAdapter deviceAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private Menu menu;

    final int STATE_BT_IDLE = 0;
    final int STATE_BT_DISCOVERING = 1;

    private int state = STATE_BT_IDLE;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        deviceViewModel =
                ViewModelProviders.of(this).get(DeviceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        final TextView textView = root.findViewById(R.id.text_device_title);
//        deviceViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        textView.setText((String) deviceViewModel.get("text"));
        // hide text view
        textView.setVisibility(View.GONE);

        setHasOptionsMenu(true);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();

//        deviceListViewAdapter = new ArrayAdapter(requireContext(),
//                android.R.layout.simple_list_item_1,
//                deviceList);
        deviceAdapter = new ScanDeviceAdapter(requireContext(), R.layout.scan_device_item);
        deviceListView = (ListView) root.findViewById(R.id.device_list);
        deviceListView.setAdapter(deviceAdapter);
        deviceListView.setOnItemClickListener(itemClickListener);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.device, menu);
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.discover_device:
                Toast.makeText(getActivity(), "Discover", Toast.LENGTH_LONG).show();
                // item.setTitle(item.getTitle() + "#");
                // refreshDevice();
                scanLeDevice();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void scanLeDevice() {
        ((BlueSensorApplication) requireActivity().getApplication()).getBleEngine().scanLeDevice(leScanCallback);
        state = STATE_BT_DISCOVERING;
        requireActivity().invalidateOptionsMenu();
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

//            BluetoothDevice device = result.getDevice();
//            String deviceName = device.getName();
//            deviceList.add((deviceName == null ? "" : deviceName) + "\n" + device.getAddress());
//            Toast.makeText(requireContext(), device.getAddress(), Toast.LENGTH_LONG).show();
//            deviceListViewAdapter.notifyDataSetChanged();
            deviceAdapter.addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItemDiscover = menu.findItem(R.id.discover_device);
        switch (state) {
            case STATE_BT_IDLE:
                menuItemDiscover.setTitle(R.string.discover);
                menuItemDiscover.setEnabled(true);
                break;
            case STATE_BT_DISCOVERING:
                menuItemDiscover.setTitle(R.string.searching);
                menuItemDiscover.setEnabled(false);
                break;
        }
    }

    private void refreshDevice() {
        bluetoothAdapter.startDiscovery();
        state = STATE_BT_DISCOVERING;
        requireActivity().invalidateOptionsMenu();
        Toast.makeText(requireContext(), "Discovering", Toast.LENGTH_LONG).show();
    }

//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            // When discovery finds a device
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                String deviceName = device.getName();
//                deviceList.add((deviceName == null ? "" : deviceName) + "\n" + device.getAddress());
//                //Toast.makeText(requireContext(), device.getAddress(), Toast.LENGTH_LONG).show();
//                deviceListViewAdapter.notifyDataSetChanged();
//            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                //do something else
//                state = STATE_BT_IDLE;
//                requireActivity().invalidateOptionsMenu();
//            }
//        }
//    };

    private static final int REQUEST_ENABLE_BT = 111;

    @Override
    public void onResume() {
        if (!bluetoothAdapter.isEnabled()) {
            requireActivity().startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT);
        }

//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
//        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
//
//        requireActivity().registerReceiver(broadcastReceiver, intentFilter);

//        if (state == STATE_BT_IDLE)
//            refreshDevice();

        if (((BlueSensorApplication)requireActivity().getApplication()).getBleEngine().isLeScanning()) {
            state = STATE_BT_DISCOVERING;
        } else {
            state = STATE_BT_IDLE;
        }
        requireActivity().invalidateOptionsMenu();
        Log.d(TAG, "state=" + state);

        super.onResume();
    }

    @Override
    public void onPause() {
//        requireActivity().unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ///Toast.makeText(requireContext(), deviceList.get(position), Toast.LENGTH_LONG).show();
//            String[] lines = deviceList.get(position).split("\\r?\\n");
            //for (String line : lines) {
            //Toast.makeText(getContext(), line + "LENGTH:" + lines.length, Toast.LENGTH_LONG).show();
            //}
            //Toast.makeText(getContext(), lines[0], Toast.LENGTH_LONG).show();
            //Toast.makeText(getContext(), lines.toString() + lines.length, Toast.LENGTH_LONG).show();
//            String address = lines[1];
//            (new BluetoothConnection(null, address)).start();
            Toast.makeText(requireContext(), deviceAdapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScanLeStopEvent event) {
        state = STATE_BT_IDLE;
        requireActivity().invalidateOptionsMenu();
    }
}