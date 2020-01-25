package gr.invision.gpstracker.gpstracker.googleapi2;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.ref.WeakReference;

public class GoogleLocation2 extends LocationCallback implements OnSuccessListener<Location>, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long UPDATE_INTERVAL = 15000;  /* 15 secs */
    private static final long FASTEST_INTERVAL = 5000; /* 5 secs */

    private Location mLocation;
    private WeakReference<Context> contextWeakReference;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public GoogleLocation2(Context context) {
        contextWeakReference = new WeakReference<>(context);

        mGoogleApiClient = new GoogleApiClient.Builder(contextWeakReference.get())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(contextWeakReference.get());

        onStart();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onSuccess(Location location) {

    }

    private void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, this, null);


        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    public void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            fusedLocationProviderClient.removeLocationUpdates(this);
            mGoogleApiClient.disconnect();
        }
    }
}
