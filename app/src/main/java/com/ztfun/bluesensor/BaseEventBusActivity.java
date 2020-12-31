package com.ztfun.bluesensor;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

public class BaseEventBusActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
