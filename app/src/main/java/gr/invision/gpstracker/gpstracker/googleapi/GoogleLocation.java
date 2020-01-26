package gr.invision.gpstracker.gpstracker.googleapi;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.ref.WeakReference;

public class GoogleLocation extends LocationCallback implements OnSuccessListener<Location>, LocationListener {

    public interface OnLocationUpdateListener {
        void getGoogleLocationUpdate(Location location);
    }

    private static final long UPDATE_INTERVAL = 7000;
    private static final long FASTEST_INTERVAL = 1000;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private OnLocationUpdateListener onLocationUpdateListener;

    private static GoogleLocation INSTANCE;

    private GoogleLocation(Context context, OnLocationUpdateListener onLocationUpdateListener) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        this.onLocationUpdateListener = onLocationUpdateListener;
        init(contextWeakReference.get());
    }

    public synchronized static GoogleLocation getInstance(Context context, OnLocationUpdateListener onLocationUpdateListener) {
        if (INSTANCE == null) {
            INSTANCE = new GoogleLocation(new WeakReference<>(context).get(), onLocationUpdateListener);
        }
        return INSTANCE;
    }

    public void destroyInstance() {
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
        onLocationChanged(locationResult.getLastLocation());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            onLocationUpdateListener.getGoogleLocationUpdate(location);
        }
    }


    private void init(Context context) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this);
        fusedLocationProviderClient.flushLocations();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, this, Looper.myLooper());
    }
}
