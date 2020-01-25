package gr.invision.gpstracker.gpstracker.googleapi;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

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

    public interface OnLocationUpdate {
        void getGoogleLocationUpdate(Location location);
    }

    private static final long UPDATE_INTERVAL = 15000;  /* 15 secs */

    private static final long FASTEST_INTERVAL = 5000; /* 5 secs */

    private FusedLocationProviderClient fusedLocationProviderClient;

    private OnLocationUpdate onLocationUpdate;

    public GoogleLocation(Context context) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        startLocationUpdates(contextWeakReference.get());
    }

    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            //Log.e("LAT - LONG", String.valueOf(location.getLongitude()) + location.getLatitude());
        }
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        onLocationChanged(locationResult.getLastLocation());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            onLocationUpdate.getGoogleLocationUpdate(location);
            Log.e("LAT - LONG", String.valueOf(location.getLongitude()) + location.getLatitude());
        }
    }

    public void registerLocationListener(OnLocationUpdate onLocationUpdate) {
        this.onLocationUpdate = onLocationUpdate;
    }

    private void startLocationUpdates(Context context) {
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

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(this);
    }
}
