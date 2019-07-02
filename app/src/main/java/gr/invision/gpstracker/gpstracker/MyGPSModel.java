package gr.invision.gpstracker.gpstracker;

import android.location.Location;

class MyGPSModel {

    private Location location;
    private GPSManager.GpsStatus gpsStatus;

    MyGPSModel(Location location, GPSManager.GpsStatus gpsStatus) {
        this.location = location;
        this.gpsStatus = gpsStatus;
    }

    Location getLocation() {
        return location;
    }

    GPSManager.GpsStatus getGpsStatus() {
        return gpsStatus;
    }
}
