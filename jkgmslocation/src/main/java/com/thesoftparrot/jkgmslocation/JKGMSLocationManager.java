package com.thesoftparrot.jkgmslocation;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class JKGMSLocationManager implements JKGMSLocationService.LocationLiveUpdateCallback {

    private static final String TAG = "JKGMSLocationManager";
    private static final int RESOLVABLE_ERROR_REQUEST_CODE = 8766;

    private static JKGMSLocationManager instance;

    public static JKGMSLocationManager getInstance() {
        if(instance == null)
            instance = new JKGMSLocationManager();
        return instance;
    }

    private LocationRequest mLocationRequest;
    private int interval, fastestInterval, minDisplacement;

    private JKGMSLocationManager() {
        if (mLocationRequest == null)
            mLocationRequest = new LocationRequest();

        interval = 1000;
        fastestInterval = 2000;
        minDisplacement = 0;

        initLiveData();
        JKGMSLocationService.setLiveLocationCallback(this);
    }

    private MutableLiveData<Location> mLocationLiveData;

    public LiveData<Location> onLocationUpdatedLiveData(){
        if(mLocationLiveData == null)
            mLocationLiveData = new MutableLiveData<>();

        return mLocationLiveData;
    }

    private void initLiveData() {
        if(mLocationLiveData == null)
            mLocationLiveData = new MutableLiveData<>();
    }

    // Will enable GPS programmatically if not enabled, and start location updates
    public void requestGpsLocationUpdates(final Activity activity, int appIcon) {
        initLocationRequest();
        enableGps(activity);
        startJKGMSLocationService(activity, appIcon);
    }

    // Will enable GPS programmatically if not enabled, and start location updates
    public void requestGpsLocationUpdates(final Activity activity, int appIcon, int interval, int fastestInterval, int minDisplacement) {
        this.interval = interval;
        this.fastestInterval = fastestInterval;
        this.minDisplacement = minDisplacement;

        initLocationRequest();
        enableGps(activity);
        startJKGMSLocationService(activity, appIcon);
    }

    public void removeGpsLocationUpdates(Activity activity){
        stopJKGMSLocationService(activity);
    }

    private void enableGps(final Activity activity) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                Log.d(TAG, "onComplete_Task: " + task.getException());

                try {
                    task.getResult(ApiException.class);
                } catch (ApiException error) {
                    switch (error.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) error;
                            try {
                                resolvableApiException.startResolutionForResult(activity, RESOLVABLE_ERROR_REQUEST_CODE);
                            } catch (IntentSender.SendIntentException | ClassCastException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                            break;
                        }
                    }
                }
            }
        });
    }

    private void startJKGMSLocationService(Activity activity, int icon) {
        if(!isLocationServiceAlreadyRunning(activity)){
            Intent intent = new Intent(activity, JKGMSLocationService.class);
            intent.setAction(JKGMSConstants.ACTION_START_JK_GMS_LOCATION_SERVICE);
            intent.putExtra(JKGMSConstants.KEY_APP_ICON, icon);
            intent.putExtra(JKGMSConstants.KEY_INTERVAL, interval);
            intent.putExtra(JKGMSConstants.KEY_FASTEST_INTERVAL, fastestInterval);
            intent.putExtra(JKGMSConstants.KEY_MIN_DISPLACEMENT, minDisplacement);
            activity.startService(intent);
        }
    }

    private void stopJKGMSLocationService(Activity activity) {
        if(isLocationServiceAlreadyRunning(activity)){
            Intent intent = new Intent(activity, JKGMSLocationService.class);
            intent.setAction(JKGMSConstants.ACTION_STOP_JK_GMS_LOCATION_SERVICE);
            activity.stopService(intent);
        }
    }

    private void initLocationRequest() {

        if (mLocationRequest == null)
            mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setSmallestDisplacement(minDisplacement);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean isLocationServiceAlreadyRunning(Context activity) {

        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceInfo != null) {
                    if (JKGMSLocationService.class.getName().equals(serviceInfo.service.getClassName())) {
                        if (serviceInfo.foreground)
                            return true;
                    }
                }
            }

            return false;
        }

        return false;
    }

    @Override
    public void onLiveLocationUpdated(Location location) {
        mLocationLiveData.setValue(location);
    }
}
