package com.adrian.sofergt.services;

import android.location.Location;

class SendLocationToActivity {

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private Location location;

    public SendLocationToActivity(Location mLocation) {

    }
}
