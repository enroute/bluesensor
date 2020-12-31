package com.ztfun.bluesensor;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ztfun.bluesensor.model.BleEngine;
import com.ztfun.bluesensor.ui.fslive.FullscreenLiveFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // ignore re-selection
        navView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                // do nothing
            }
        });

        setProgressBarIndeterminate(false);

        // request permission for BLE
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        BleEngine.enableBle(this, 12345);

        // start service if necessary
        ((BlueSensorApplication)getApplication()).startBleService();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the fragment's instance
        // getSupportFragmentManager().putFragment(outState, "myFragmentName", mMyFragment);
    }

    public void navigateTo(int resId) {
        switch (resId) {
            case R.id.navigation_home:
            case R.id.navigation_dashboard:
            case R.id.navigation_notifications:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(resId);
                return;

            case R.id.navigation_fullscreen:
                Fragment fragment = new FullscreenLiveFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        //.add(R.id.nav_host_fragment, fragment)
                        .replace(R.id.nav_host_fragment, fragment)
                        .commit();
                // hide BottomNavigationView
                findViewById(R.id.nav_view).setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (fragment instanceof FullscreenLiveFragment) {
            //navigateTo(R.id.navigation_home);
            getSupportFragmentManager().popBackStack();
            // reset full screen
            // show BottomNavigationView
            //findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
            return;
        } else {
            Toast.makeText(this, "super.onBackPressed", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }
}