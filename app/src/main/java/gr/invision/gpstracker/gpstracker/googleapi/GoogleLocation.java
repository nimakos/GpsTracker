package gr.invision.gpstracker.gpstracker.googleapi;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.ref.WeakReference;

/**
 * Checking device's GPS settings and select the best provider
 * Call from activity like:
 * googleLocation = new GoogleLocation
 * .Builder(this, this)
 * .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
 * .setFastestInterval(17000)
 * .setUpdateInterval(11000)
 * .build();
 */
public class GoogleLocation extends LocationCallback implements OnSuccessListener<Location> {

    /**
     * Callback interface to receive GPS updates from MyGPSManager.
     */
    public interface OnLocationUpdateListener {
        void getGoogleLocationUpdate(Location location);
    }

    public interface OnSpeedUpdateListener {
        void getSpeedUpdate(float speed);
    }

    //required parameters
    private OnLocationUpdateListener onLocationUpdateListener;

    //optional parameters
    private long UPDATE_INTERVAL;
    private long FASTEST_INTERVAL;
    private int PRIORITY;
    private OnSpeedUpdateListener onSpeedUpdateListener;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final float MPS_to_KPH = 3.6f;
    private static GoogleLocation INSTANCE;

    public static class Builder {

        //required parameters
        private OnLocationUpdateListener onLocationUpdateListener;
        private Context context;

        //optional parameters
        private int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        private long update_interval = 1000 * 2;
        private long fastest_interval = 1000;
        private OnSpeedUpdateListener onSpeedUpdateListener = null;

        /**
         * The Builder constructor
         *
         * @param context                  The Activity context
         * @param onLocationUpdateListener The Observer location listener
         */
        public Builder(Context context, OnLocationUpdateListener onLocationUpdateListener) {
            this.context = context;
            this.onLocationUpdateListener = onLocationUpdateListener;
        }

        public Builder setUpdateInterval(long update_interval) {
            this.update_interval = update_interval;

            return this;
        }

        public Builder setFastestInterval(long fastestInterval) {
            this.fastest_interval = fastestInterval;

            return this;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;

            return this;
        }

        public Builder setSpeedListener(OnSpeedUpdateListener speedListener) {
            this.onSpeedUpdateListener = speedListener;

            return this;
        }

        public GoogleLocation build() {
            return getInstance(this);
        }
    }

    /**
     * The private main class constructor
     *
     * @param builder The builder class
     */
    private GoogleLocation(Builder builder) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(builder.context);
        this.onLocationUpdateListener = builder.onLocationUpdateListener;
        this.UPDATE_INTERVAL = builder.update_interval;
        this.FASTEST_INTERVAL = builder.fastest_interval;
        this.PRIORITY = builder.priority;
        this.onSpeedUpdateListener = builder.onSpeedUpdateListener;
        init(contextWeakReference.get());
    }

    /**
     * Singleton pattern. This constructor creates only one instance
     *
     * @param builder The Builder class
     * @return This single instance
     */
    private static GoogleLocation getInstance(Builder builder) {
        if (INSTANCE == null) {
            synchronized (GoogleLocation.class) {
                INSTANCE = new GoogleLocation(builder);
            }
        }
        return INSTANCE;
    }

    /**
     * This destructor destroys all instances and removes location updates
     */
    public void destroyInstance() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(this);
        fusedLocationProviderClient = null;
        onLocationUpdateListener = null;
        INSTANCE = null;
    }

    @Override
    public void onSuccess(Location location) {
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        for (Location location : locationResult.getLocations()) {
            if (location != null) {
                onLocationUpdateListener.getGoogleLocationUpdate(location);
                if (onSpeedUpdateListener != null) {
                    if (location.hasSpeed())
                        onSpeedUpdateListener.getSpeedUpdate(location.getSpeed() * MPS_to_KPH);
                    else onSpeedUpdateListener.getSpeedUpdate(0.0f);
                }
            } else {
                if (onSpeedUpdateListener != null)
                    onSpeedUpdateListener.getSpeedUpdate(0.0f);
            }
        }
    }

    /**
     * initialize provider
     *
     * @param context The activity context
     */
    private void init(Context context) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(PRIORITY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //it ask you if you agree with google the first time only
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this);
        fusedLocationProviderClient.flushLocations();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, this, Looper.myLooper());
    }
}