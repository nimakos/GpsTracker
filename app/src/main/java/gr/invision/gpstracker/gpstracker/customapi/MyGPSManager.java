package gr.invision.gpstracker.gpstracker.customapi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

/**
 * Check device's GPS settings and select the best provider
 * Call from main like:  MyGPSManager gpsManager = MyGPSManager.getInstance(this);
 * along with:           gpsManager.init(this, this);
 */
public class MyGPSManager implements GpsStatus.Listener, LocationListener {

    /**
     * Callback interface to receive GPS updates from MyGPSManager.
     */
    public interface GPSListener {
        void getLocationUpdate(Location location);
        void getSpeedUpdate(float speed);
        void onGpsNetworkStatusUpdate(String status);
        void getLocationAsynchronousUpdate(Location location);
    }

    public enum GpsStatus {
        FAILED,
        GPS_STARTED,
        NETWORK_STARTED,
    }

    /**
     * Helper class
     */
    public static class MyGPSModel {
        private Location location;
        private GpsStatus gpsStatus;

        MyGPSModel(Location location, GpsStatus gpsStatus) {
            this.location = location;
            this.gpsStatus = gpsStatus;
        }

        Location getLocation() {
            return location;
        }

        GpsStatus getGpsStatus() {
            return gpsStatus;
        }
    }

    private static int myMinTime;
    private static int myMinDistance;
    private boolean isGPSEnabled, isNetworkEnabled;
    private LocationManager locationManager;
    private GPSListener gpsListener;
    private WeakReference<Context> contextWeakReference;
    private MyGPSModel myGPSModel;
    private static final float MPS_to_KPH = 3.6f;

    private static MyGPSManager INSTANCE;

    private MyGPSManager(Context context, GPSListener gpsListener) {
        contextWeakReference = new WeakReference<>(context);
        this.gpsListener = gpsListener;
        init();
    }

    /**
     * Static accessor (Singleton pattern)
     *
     * @return instance
     */
    public synchronized static MyGPSManager getInstance(Context context, GPSListener gpsListener, int minTime, int minDistance) {
        if (INSTANCE == null) {
            INSTANCE = new MyGPSManager(new WeakReference<>(context).get(), gpsListener);
        }
        myMinTime = minTime;
        myMinDistance = minDistance;
        return INSTANCE;
    }

    /**
     * Όταν η εφαρμογή φύγει από το MainActivity π.χ. πάει στο παρασκήνιο
     */
    public void destroyInstance() {
        locationManager.removeUpdates(this);
        gpsListener = null;
        locationManager = null;
        INSTANCE = null;
    }

    private void init() {
        initLocationManager();
        checkIfNetworkOrGpsEnabled();
        getLastKnownLocation();
        promptGpsEnable();
    }

    private void initLocationManager() {
        locationManager = (LocationManager) contextWeakReference.get().getSystemService(Context.LOCATION_SERVICE);
    }

    private void checkIfNetworkOrGpsEnabled() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(contextWeakReference.get(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(contextWeakReference.get(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) contextWeakReference.get(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, myMinTime, myMinDistance, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, myMinTime, myMinDistance, this);

        GpsStatus gpsStatus;
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = location;
            }
        }
        if (bestLocation == null) {
            gpsStatus = GpsStatus.FAILED;
            myGPSModel = new MyGPSModel(null, gpsStatus);
        } else {
            if (bestLocation.getProvider().equals("gps")) {
                gpsStatus = GpsStatus.GPS_STARTED;
                myGPSModel = new MyGPSModel(bestLocation, gpsStatus);
            } else {
                gpsStatus = GpsStatus.NETWORK_STARTED;
                myGPSModel = new MyGPSModel(bestLocation, gpsStatus);
            }
        }
    }

    private void promptGpsEnable() {
        switch (myGPSModel.getGpsStatus()) {
            case FAILED:
                gpsListener.onGpsNetworkStatusUpdate("Something went wrong...");
                break;
            case GPS_STARTED:
                gpsListener.onGpsNetworkStatusUpdate("Gps Location Started");
                break;
            case NETWORK_STARTED:
                gpsListener.onGpsNetworkStatusUpdate("Network Location Started");
                break;
        }
    }

    public void onGpsStatusChanged(int event) {
        int Satellites = 0;
        int SatellitesInFix = 0;
        if (ActivityCompat.checkSelfPermission(contextWeakReference.get(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(contextWeakReference.get(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) contextWeakReference.get(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        int timeToFix = Objects.requireNonNull(locationManager.getGpsStatus(null)).getTimeToFirstFix();
        Log.i("GPs", "Time to first fix = " + timeToFix);
        for (GpsSatellite sat : Objects.requireNonNull(locationManager.getGpsStatus(null)).getSatellites()) {
            if (sat.usedInFix()) {
                SatellitesInFix++;
            }
            Satellites++;
        }
        Log.i("GPS", Satellites + " Used In Last Fix (" + SatellitesInFix + ")");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            gpsListener.getLocationUpdate(location);
            gpsListener.getSpeedUpdate(location.getSpeed() * MPS_to_KPH);
        } else {
            gpsListener.getSpeedUpdate(0.0f);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals("gps") && !isGPSEnabled)
            showSettingsAlert(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (provider.equals("network") && !isNetworkEnabled && isGPSEnabled)
            showSettingsAlert(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
    }

    /**
     * Μεταβαση στο μενου για χειροκίνητο ανοιγμα από τον χρήστη
     */
    private void showSettingsAlert(String settings) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(contextWeakReference.get());
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to enable it?");
        alertDialog.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(settings);
            contextWeakReference.get().startActivity(intent);
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }


    // ============================= Un-used methods ==================================

    /**
     * Παίρνει την τρέχουσα τοποθεσία είτε μέσω Network είτε μέσω GPS για κάθε περίπτωση
     */
    private MyGPSModel getLastKnownLocationTest() {
        Location networkLocation = null, gpsLocation = null, finalLocation = null;
        GpsStatus gpsStatus;

        if (ActivityCompat.checkSelfPermission(contextWeakReference.get(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(contextWeakReference.get(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) contextWeakReference.get(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, myMinTime, myMinDistance, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, myMinTime, myMinDistance, this);

        try {
            if (isNetworkEnabled) {
                if (locationManager != null)
                    networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (isGPSEnabled) {
                if (locationManager != null)
                    gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } catch (Exception ignore) {
        }

        if (gpsLocation != null && networkLocation != null) {
            //smaller the number more accurate result will
            if (gpsLocation.getAccuracy() > networkLocation.getAccuracy()) {
                finalLocation = networkLocation;
                gpsStatus = GpsStatus.NETWORK_STARTED;
            } else {
                finalLocation = gpsLocation;
                gpsStatus = GpsStatus.GPS_STARTED;
            }
        } else {
            if (gpsLocation != null) {
                finalLocation = gpsLocation;
                gpsStatus = GpsStatus.GPS_STARTED;
            } else if (networkLocation != null) {
                finalLocation = networkLocation;
                gpsStatus = GpsStatus.NETWORK_STARTED;
            } else {
                gpsStatus = GpsStatus.FAILED;
            }
        }
        myGPSModel = new MyGPSModel(finalLocation, gpsStatus);
        return myGPSModel;
    }

    /**
     * Αυτόματη επιλογή του κατάλληλου Provider
     * <p>
     * If you use ACCURACY_FINE, you will use GPS only,
     * but GPS have "cold start" effect. It means that if you device not used GPS long time,
     * it needs a lot of time for connecting with satellites and download almanac and ephemeris.
     * You can't change this behavior.
     * <p>
     * If you don't use ACCURACY_FINE, you will use both GPS and network (mobile, wi-fi) signals.
     * So you will receive quick first position from network, because they don't have "cold start" effect,
     * but accuracy of this method is low. When your GPS module will ready,
     * you will start receive updates from it too.
     */
    private String getBestProvider() {
        String bestProvider;

        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        bestProvider = locationManager.getBestProvider(criteria, false);

        if (bestProvider != null && bestProvider.length() > 0) {
            return bestProvider;
        } else {
            final List<String> providers = locationManager.getProviders(true);
            for (String provider : providers) {
                bestProvider = provider;
            }
            return bestProvider;
        }
    }

    private void startNewLocation() {
        new Thread(() -> {
            while (true) {
                Location location = getLastKnownLocationTest().getLocation();
                if (location != null) {
                    gpsListener.getLocationAsynchronousUpdate(location);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}