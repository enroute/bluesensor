package com.ztfun.bluesensor;

import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;

public class BaseEventBusFragment extends Fragment {
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
}
