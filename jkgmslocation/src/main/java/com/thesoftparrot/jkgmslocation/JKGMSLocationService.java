package com.thesoftparrot.jkgmslocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class JKGMSLocationService extends Service {

    private LocationRequest mLocationRequest;
    private static LocationLiveUpdateCallback mLiveLocationCallback;

    public static void setLiveLocationCallback(LocationLiveUpdateCallback listener) {
        mLiveLocationCallback = listener;
    }

    private int appIcon, interval = 1000, fastestInterval = 2000, minDisplacement = 0;

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult != null && locationResult.getLastLocation() != null) {
                if(mLiveLocationCallback != null)
                    mLiveLocationCallback.onLiveLocationUpdated(locationResult.getLastLocation());
            }
        }
    };

    public JKGMSLocationService() {
        mLocationRequest = new LocationRequest();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            appIcon = intent.getIntExtra(JKGMSConstants.KEY_APP_ICON, -1);
            interval = intent.getIntExtra(JKGMSConstants.KEY_INTERVAL, 1000);
            fastestInterval = intent.getIntExtra(JKGMSConstants.KEY_FASTEST_INTERVAL, 1000);
            minDisplacement = intent.getIntExtra(JKGMSConstants.KEY_MIN_DISPLACEMENT, 0);

            String action = intent.getAction();

            if (action != null && !TextUtils.isEmpty(action)) {
                if (action.equalsIgnoreCase(JKGMSConstants.ACTION_START_JK_GMS_LOCATION_SERVICE)) {
                    startJKGMSLocationService(appIcon, interval, fastestInterval, minDisplacement);
                } else if (action.equalsIgnoreCase(JKGMSConstants.ACTION_STOP_JK_GMS_LOCATION_SERVICE)) {
                    stopJKGMSLocationService();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("MissingPermission")
    private void startJKGMSLocationService(int appIcon, int interval, int fastestInterval, int minDisplacement) {
        NotificationCompat.Builder builder = initNotificationCompatBuilder(appIcon);
        initLocationRequest(interval, fastestInterval, minDisplacement);

        LocationServices
                .getFusedLocationProviderClient(this)
                .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());

        startForeground(JKGMSConstants.JK_GMS_LOCATION_SERVICE_ID, builder.build());
    }

    private NotificationCompat.Builder initNotificationCompatBuilder(int smallIcon) {
        String channelId = "jk_gms_location_channel_id";

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        builder.setSmallIcon(smallIcon);
        builder.setAutoCancel(false);
        builder.setContentTitle("Location Service");
        builder.setContentText("Running");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        initNotificationManager(channelId);

        return builder;
    }

    private void initNotificationManager(String channelId) {

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification Channel for Oreo or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager != null && manager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Used by Location Service");
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void initLocationRequest(int interval, int fastestInterval, int smallestDisplacement) {

        if (mLocationRequest == null)
            mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setSmallestDisplacement(smallestDisplacement);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void stopJKGMSLocationService() {
        LocationServices
                .getFusedLocationProviderClient(this)
                .removeLocationUpdates(mLocationCallback);
        stopForeground(true);
        stopSelf();
    }

    public interface LocationLiveUpdateCallback {
        void onLiveLocationUpdated(Location location);
    }
}
