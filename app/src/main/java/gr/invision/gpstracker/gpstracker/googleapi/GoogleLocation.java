package gr.invision.gpstracker.gpstracker.googleapi;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.ref.WeakReference;

public class GoogleLocation implements OnSuccessListener<Location> {

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public GoogleLocation(Context context) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(7500);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(contextWeakReference.get());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this);
        fusedLocationProviderClient.flushLocations();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // Logic to handle location object
                    }
                }
            }
        };
    }


    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            // Logic to handle location object
        }
    }

    public void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
