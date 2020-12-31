package com.ztfun.bluesensor.ui.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ztfun.bluesensor.ConnectActivity;
import com.ztfun.bluesensor.R;
import com.ztfun.bluesensor.events.ConnectEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ScanDeviceAdapter extends ArrayAdapter {
    private int mLayoutResource;
    private List<ScanResult> scanResult = new ArrayList<>();

    public ScanDeviceAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mLayoutResource = resource;
    }

    public void addScanResult(ScanResult result) {
        int index = getResultIndex(result.getDevice());
        if (index >= 0) {
            scanResult.set(index, result);
        } else {
            scanResult.add(result);
        }
        notifyDataSetChanged();
    }

    private int getResultIndex(BluetoothDevice device) {
        String address = device.getAddress();
        for (int i = 0; i < scanResult.size(); i++) {
            if (address.equals(scanResult.get(i).getDevice().getAddress())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getCount() {
        return scanResult.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        // return super.getItem(position);
        return scanResult.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view= LayoutInflater.from(getContext()).inflate(mLayoutResource, parent, false);

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder = new ViewHolder();
            viewHolder.icon = view.findViewById(R.id.scan_item_device_icon);
            viewHolder.name = view.findViewById(R.id.scan_item_device_name);
            viewHolder.address = view.findViewById(R.id.scan_item_device_address);
            viewHolder.boundStatus = view.findViewById(R.id.scan_item_device_bound_status);
            viewHolder.connectible = view.findViewById(R.id.scan_item_device_connectible);
            viewHolder.signal = view.findViewById(R.id.scan_item_device_signal);
            viewHolder.connect = view.findViewById(R.id.scan_item_connect);

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.setTag(viewHolder);
        } else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        // update text
        BluetoothDevice device = scanResult.get(position).getDevice();
        final String address = device.getAddress();
        String name = device.getName();
        if (name == null || name.equals("")) {
            name = "N/A";
        }
        viewHolder.name.setText(name);
        viewHolder.address.setText(address);

        switch (device.getBondState()) {
            case BluetoothDevice.BOND_NONE:
                viewHolder.boundStatus.setText(R.string.not_bound);
                break;

            case BluetoothDevice.BOND_BONDED:
                viewHolder.boundStatus.setText("Bound");
                break;

            case BluetoothDevice.BOND_BONDING:
                viewHolder.boundStatus.setText("Bounding");
                break;
        }

        // connectible or not?
        boolean connectible = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectible = scanResult.get(position).isConnectable();
        } else {
            int flags = scanResult.get(position).getScanRecord().getAdvertiseFlags();
            connectible = ((flags & 2) == 2) ;
        }
        viewHolder.connectible.setText(connectible ? "Connectible" : "Not connectible");

        // signal
        viewHolder.signal.setText(scanResult.get(position).getRssi() + "dBm");

        viewHolder.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EventBus.getDefault().post(new ConnectEvent(address));
                Intent intent = new Intent(getContext(), ConnectActivity.class);
                intent.putExtra("address", address);
                getContext().startActivity(intent);
            }
        });

        return view;
    }

    // 定义一个内部类，用于对控件的实例进行缓存
    class ViewHolder{
        ImageView icon;
        TextView name;
        TextView address;
        TextView boundStatus;
        TextView connectible;
        TextView signal;
        Button connect;
    }
}
