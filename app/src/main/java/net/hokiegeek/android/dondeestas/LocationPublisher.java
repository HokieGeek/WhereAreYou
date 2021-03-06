package net.hokiegeek.android.dondeestas;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andres on 11/29/16.
 */

public class LocationPublisher extends Service
{
    private List<LocationListener> listeners;

    private LocationTracker tracker;

    private Context context;

    private class LocationTracker
            implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            LocationListener
    {
        private GoogleApiClient googleApiClient;

        private LocationRequest locationRequest;

        LocationTracker() {
            listeners = new ArrayList<>();

            locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        void enable(boolean enable) {
            Log.v(Util.TAG, "LocationTracker.enable("+enable+")");
            if (enable) {
                googleApiClient.connect();
            } else {
                googleApiClient.disconnect();
            }
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            // TODO: Verify location settings
        /*
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>()) {
             @Override
             public void onResult(LocationSettingsResult result) {
                 final Status status = status.getStatus();
                 final LocationSettingsStates = result.getLocationSettingsStates();
                 switch (status.getStatusCode()) {
                     case LocationSettingsStatusCodes.SUCCESS:
                         // All location settings are satisfied. The client can
                         // initialize location requests here. ...
                         break;
                     case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                         // Location settings are not satisfied, but this can be fixed
                         // by showing the user a dialog.
                         try {
                             // Show the dialog by calling startResolutionForResult(),
                             // and check the result in onActivityResult().
                             status.startResolutionForResult(
                                 OuterClass.this,
                                 REQUEST_CHECK_SETTINGS);
                         } catch (IntentSender.SendIntentException e) {
                             // Ignore the error.
                         }
                         break;
                     case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                         // Location settings are not satisfied. However, we have no way
                         // to fix the settings so we won't show the dialog. ...
                         break;
                 }
             }
        });
        */

            // if (requestingLocationUpdates) {
            Log.v(Util.TAG, "Have requested location updates");
            startLocationUpdates();
            // } else {
            //     Log.v(TAG, "Not starting location updates");
            // }
        }

        void startLocationUpdates() {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(Util.TAG, "Starting location updates: have permissions");
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
                Log.e(Util.TAG, "Starting location updates: do not have permissions");
                Toast.makeText(context, "Do not have permissions", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            // TODO: implement onConnectionSuspended?
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            // TODO: implement onConnectionFailed?
        }

        @Override
        public void onLocationChanged(Location location) {
            fireOnLocationChanged(location);
        }
    }

    public LocationPublisher() {
        listeners = new ArrayList<>();
    }

    public LocationPublisher(AppCompatActivity parent) {
        this();
        context = parent;
        tracker = new LocationTracker();
    }

    public void addListener(LocationListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public boolean removeListener(LocationListener l) {
        synchronized (listeners) {
            if (listeners.contains(l)) {
                listeners.remove(l);
                return true;
            }
        }
        return false;
    }

    private void fireOnLocationChanged(Location loc) {
        for (LocationListener l : listeners) {
            l.onLocationChanged(loc);
        }
    }

    public void enable(boolean enable) {
        if (tracker != null) {
            tracker.enable(enable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        this.addListener((LocationListener)context); // TODO: ?

        tracker = new LocationTracker();

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}
