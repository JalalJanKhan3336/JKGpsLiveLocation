package com.thesoftparrot.gmslocationupdatestest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.Observer;

import com.thesoftparrot.gmslocationupdatestest.databinding.ActivityMainBinding;
import com.thesoftparrot.jkgmslocation.JKGMSConstants;
import com.thesoftparrot.jkgmslocation.JKGMSLocationManager;
import com.thesoftparrot.jkgmslocation.JKGMSLocationService;
import com.thesoftparrot.jkpm.JKPMActivity;

public class MainActivity extends JKPMActivity {

    private ActivityMainBinding mBinding;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private JKGMSLocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mLocationManager = JKGMSLocationManager.getInstance();

        click();
        observeLiveLocation();
    }

    private void observeLiveLocation() {
        mLocationManager.onLocationUpdatedLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if(location != null){
                    mBinding.latitudeTv.setText(String.valueOf(location.getLatitude()));
                    mBinding.longitudeTv.setText(String.valueOf(location.getLongitude()));
                }
            }
        });
    }

    private void click() {
        mBinding.startLocationUpdatesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(areAllPermissionsEnabled(PERMISSIONS))
                    startLiveLocationService();
                else
                    askRuntimePermissions(PERMISSIONS);
            }
        });

        mBinding.stopLocationUpdatesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLiveLocationService();
            }
        });
    }

    private void stopLiveLocationService() {
        mLocationManager.removeGpsLocationUpdates(this);
    }

    private void startLiveLocationService() {
        mLocationManager.requestGpsLocationUpdates(this, R.drawable.ic_launcher_background);
    }

    @Override
    protected void onPermissionsGranted() {
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        startLiveLocationService();
    }

    @Override
    protected void onPermissionsDenied() {
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        showDenialBox();
    }

}