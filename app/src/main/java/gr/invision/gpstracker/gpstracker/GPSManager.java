package gr.invision.gpstracker.gpstracker;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.List;

/**
 * Check device's GPS settings and select the best provider
 * Call from main like:  GPSManager gpsManager = GPSManager.getInstance(this);
 * along with:           gpsManager.setGPSListener(this);
 */
public class GPSManager implements GpsStatus.Listener, LocationListener {

    private static int myMinTime;
    private static int MyMinDistance;
    private boolean isGPSEnabled, isNetworkEnabled = false;
    private LocationManager locationManager;
    private GPSListener gpsListener;
    private Context mContext;
    private MyGPSModel myGPSModel;
    private final float MPS_to_KPH = 3.6f;

    // Instance
    @SuppressLint("StaticFieldLeak")
    private static GPSManager mInstance = null;

    public enum GpsStatus {
        FAILED,
        GPS_STARTED,
        NETWORK_STARTED,
    }

    //Constructor
    private GPSManager(Context context) {
        this.mContext = context;
    }

    /**
     * Static accessor (Singleton pattern)
     *
     * @return instance
     */
    public static GPSManager getInstance(Context context, int minTime, int minDistance) {
        if (mInstance == null) {
            mInstance = new GPSManager(context);
        }
        myMinTime = minTime;
        MyMinDistance = minDistance;
        return mInstance;
    }

    public static GPSManager getInstance(Context context) {
        return getInstance(context, 0, 0);
    }

    /**
     * την χρησιμοποιούμε για να πάρουμε την απάντηση του Listener
     *
     * @param gpsListener Παίρνουμε τα GPS Updates από τον GPSListener
     */
    public void setGPSListener(GPSListener gpsListener) {
        this.gpsListener = gpsListener;
        enableGPS();
    }

    private void enableGPS() {
        initLocationManager();
        checkIfNetworkOrGpsEnabled();
        getLastKnownLocation2();
        promptGpsEnable();
    }

    private void initLocationManager() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    private void checkIfNetworkOrGpsEnabled() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    private void getLastKnownLocation2() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, myMinTime, MyMinDistance, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, myMinTime, MyMinDistance, this);

        GpsStatus gpsStatus;
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
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
                gpsListener.onGpsNetworkStatusChanged("Something went wrong...");
                break;
            case GPS_STARTED:
                gpsListener.onGpsNetworkStatusChanged("Gps Location Started");
                break;
            case NETWORK_STARTED:
                gpsListener.onGpsNetworkStatusChanged("Network Location Started");
                break;
        }
    }

    /**
     * Όταν η εφαρμογή φύγει από το MainActivity π.χ. πάει στο παρασκήνιο
     */
    public void stopLocation() {
        try {
            if (locationManager != null)
                locationManager.removeUpdates(this);
            locationManager = null;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }
    }

    public void onGpsStatusChanged(int event) {
        int Satellites = 0;
        int SatellitesInFix = 0;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        int timeToFix = locationManager.getGpsStatus(null).getTimeToFirstFix();
        Log.i("GPs", "Time to first fix = " + timeToFix);
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if (sat.usedInFix()) {
                SatellitesInFix++;
            }
            Satellites++;
        }
        Log.i("GPS", Satellites + " Used In Last Fix (" + SatellitesInFix + ")");
    }

    /**
     * Μεταβαση στο μενου για χειροκίνητο ανοιγμα από τον χρήστη
     */
    private void showSettingsAlert(String settings) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to enable it?");
        alertDialog.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(settings);
            mContext.startActivity(intent);
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            gpsListener.getLocation(location);
            gpsListener.getSpeed(location.getSpeed() * MPS_to_KPH);
        } else {
            gpsListener.getSpeed(0.0f);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals("gps") && !isGPSEnabled)
            showSettingsAlert(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (provider.equals("network") && !isNetworkEnabled && isGPSEnabled)
            showSettingsAlert(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
    }


    // ============================= Un-used methods ==================================

    /**
     * Παίρνει την τρέχουσα τοποθεσία είτε μέσω Network είτε μέσω GPS για κάθε περίπτωση
     */
    private MyGPSModel getLastKnownLocation1() {
        Location networkLocation = null, GpsLocation = null, finalLocation = null;
        GpsStatus gpsStatus;

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, myMinTime, MyMinDistance, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, myMinTime, MyMinDistance, this);

        try {
            if (isNetworkEnabled) {
                if (locationManager != null)
                    networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (isGPSEnabled) {
                if (locationManager != null)
                    GpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } catch (Exception ignore) {
        }

        if (GpsLocation != null && networkLocation != null) {
            //smaller the number more accurate result will
            if (GpsLocation.getAccuracy() > networkLocation.getAccuracy()) {
                finalLocation = networkLocation;
                gpsStatus = GpsStatus.NETWORK_STARTED;
            } else {
                finalLocation = GpsLocation;
                gpsStatus = GpsStatus.GPS_STARTED;
            }
        } else {
            if (GpsLocation != null) {
                finalLocation = GpsLocation;
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
                Location location = getLastKnownLocation1().getLocation();
                if (location != null) {
                    gpsListener.getLocationAsynchronous(location);
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