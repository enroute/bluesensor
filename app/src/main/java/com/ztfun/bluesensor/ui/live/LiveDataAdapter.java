package com.ztfun.bluesensor.ui.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ztfun.bluesensor.R;
import com.ztfun.bluesensor.ui.OscilloscopeView;

import java.util.ArrayList;
import java.util.List;

public class LiveDataAdapter extends ArrayAdapter {
    private int mLayoutResource;
    private List<OscilloscopeView.DataSet> mListSensorData = new ArrayList<OscilloscopeView.DataSet>();

    public LiveDataAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mLayoutResource = resource;
    }

    public void addDataSeries(OscilloscopeView.DataSet sensorData) {
        mListSensorData.add(sensorData);
    }

    @Override
    public int getCount() {
        return mListSensorData.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        // return super.getItem(position);
        return mListSensorData.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view= LayoutInflater.from(getContext()).inflate(mLayoutResource, parent, false);

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder = new ViewHolder();
            viewHolder.oscilloscopeView = view.findViewById(R.id.live_chart);

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.setTag(viewHolder);
        } else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        // 获取控件实例，并调用set...方法使其显示出来
        // viewHolder.barChart.setImageResource(fruit.getImageId());
        // viewHolder.fruitName.setText(fruit.getName());

        // register data with oscilloscopeview
        viewHolder.oscilloscopeView.registerData(mListSensorData.get(position));

        return view;
    }

    // 定义一个内部类，用于对控件的实例进行缓存
    class ViewHolder{
        OscilloscopeView oscilloscopeView;
    }
}
