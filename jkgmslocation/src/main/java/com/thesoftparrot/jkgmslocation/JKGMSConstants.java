package com.thesoftparrot.jkgmslocation;

public class JKGMSConstants {
    public static final String KEY_APP_ICON = "app_icon";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_FASTEST_INTERVAL = "fastest_interval";
    public static final String KEY_MIN_DISPLACEMENT = "min_displacement";

    private JKGMSConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Can not be initialized");
    }

    public static final int JK_GMS_LOCATION_SERVICE_ID = 3547;
    public static final String ACTION_START_JK_GMS_LOCATION_SERVICE = "startJKGMSLocationService";
    public static final String ACTION_STOP_JK_GMS_LOCATION_SERVICE = "stopJKGMSLocationService";

}
