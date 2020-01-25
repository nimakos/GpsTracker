package gr.invision.gpstracker.gpstracker;

import android.location.Location;

class MyGPSModel {

    private Location location;
    private MyGPSManager.GpsStatus gpsStatus;

    MyGPSModel(Location location, MyGPSManager.GpsStatus gpsStatus) {
        this.location = location;
        this.gpsStatus = gpsStatus;
    }

    Location getLocation() {
        return location;
    }

    MyGPSManager.GpsStatus getGpsStatus() {
        return gpsStatus;
    }
}
