package com.ztfun.bluesensor.ui.connect;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ztfun.bluesensor.BlueSensorApplication;
import com.ztfun.bluesensor.R;
import com.ztfun.bluesensor.events.CharacteristicChangedEvent;
import com.ztfun.bluesensor.events.CharacteristicReadEvent;
import com.ztfun.bluesensor.events.ConnectionStateChangeEvent;
import com.ztfun.bluesensor.events.GattServiceDiscoveredEvent;
import com.ztfun.bluesensor.model.BleEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.ztfun.Util.byte2Hex;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String address;
    private TextView tvDeviceTitle;
    private TextView tvStatus;
    private ScrollView svStatus;

    public StatusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatusFragment newInstance(String param1, String param2) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_connect_status, container, false);

        tvDeviceTitle = root.findViewById(R.id.connect_device_title);
        tvStatus = root.findViewById(R.id.connect_tv_status);
        svStatus = root.findViewById(R.id.connect_sv_status);

        Intent intent = requireActivity().getIntent();
        address = intent.getStringExtra("address");
        if (address == null) {
            tvDeviceTitle.setText("No address");
        } else {
            tvDeviceTitle.setText(address);
            appendStatus(">> Connecting");
            ((BlueSensorApplication)requireActivity().getApplication()).getBleEngine().connect(address);
        }

        return root;
    }

    private void appendStatus(String text) {
        tvStatus.setText(tvStatus.getText() + text);
        svStatus.scrollTo(0, tvStatus.getMeasuredHeight());
    }

    @Override
    public void onDestroy() {
        // EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ConnectionStateChangeEvent event) {
        appendStatus("\n" + event.getStateString());

        if (event.state == BluetoothProfile.STATE_CONNECTED) {
            appendStatus("\n>>Discovering services");
            ((BlueSensorApplication)requireActivity().getApplication()).getBleEngine().discoverServices();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CharacteristicChangedEvent event) {
        handleReadValue(event.characteristic.getValue());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CharacteristicReadEvent event) {
        handleReadValue(event.characteristic.getValue());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattServiceDiscoveredEvent event) {
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
            appendStatus("\nDiscover Gatt Service: success");
            appendStatus("\n>>Listening to characteristics");
            for (BluetoothGattService gattService :
                    ((BlueSensorApplication) requireActivity().getApplication()).getBleEngine().getGattServices()) {
                // 获取服务中的特征集合
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // 循环遍历特征集合
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                    if (gattCharacteristic.getUuid().toString().equals(BleEngine.HEART_RATE_MEASUREMENT)) {
//                        appendStatus("\n" + gattCharacteristic.getUuid());
//                        ((BlueSensorApplication) requireActivity().getApplication()).getBleEngine()
//                                .setCharacteristicNotification(gattCharacteristic);
//                    }
                    int property = gattCharacteristic.getProperties();
                    if ((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        appendStatus("\nnotifiable: " + gattCharacteristic.getUuid());
                        ((BlueSensorApplication) requireActivity().getApplication()).getBleEngine()
                                .setCharacteristicNotification(gattCharacteristic);
                    }

                    if ((property & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 ||
                            (property & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0 ) {
                        ((BlueSensorApplication) requireActivity().getApplication()).getBleEngine()
                                .setWriteCharacteristic(gattCharacteristic);
                    }
                }
            }
        } else {
            appendStatus("\nDiscover Gatt Service: " + event.status);
        }
    }

    private void handleReadValue(byte[] data) {
        // Toast.makeText(requireContext(), "handleReadValue", Toast.LENGTH_LONG).show();
        StringBuilder sb = new StringBuilder();
        sb.append("\n[RECV]");
        for (byte b : data) {
            sb.append(" ");
            sb.append(byte2Hex(b));
        }
        appendStatus(sb.toString());
    }

}