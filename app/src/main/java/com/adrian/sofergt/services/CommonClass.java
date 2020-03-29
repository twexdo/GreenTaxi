package com.adrian.sofergt.services;

import android.content.Context;
import android.location.Location;

import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CommonClass {

    public static final String KEY_REQUESTING_LOCATION_UPDATES = "LocationUpdateEnable";

    public static String getLocationText(Location mLocation){

        return mLocation == null ? "Unknown Location" : new StringBuilder()
                .append(mLocation.getLatitude())
                .append("/")
                .append(mLocation.getLongitude())
                .toString();
    }

    public static String getLocationText(MainService mainService) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return String.format("Location Updated %1$s",sdf.format(Calendar.getInstance().getTime()));

    }

    public static void setRequestLocationUpates(Context context, boolean value) {

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES,value);

    }

    public static boolean requestLocationUpates(Context context) {

        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES,false);
            }
}
