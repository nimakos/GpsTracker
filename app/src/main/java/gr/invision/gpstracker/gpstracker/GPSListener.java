package gr.invision.gpstracker.gpstracker;

import android.location.Location;

/**
 * Callback interface to receive GPS updates from MyGPSManager.
 */
public interface GPSListener {
    void getLocation(Location location);

    void getSpeed(float speed);

    void onGpsNetworkStatusChanged(String status);

    void getLocationAsynchronous(Location location);
}