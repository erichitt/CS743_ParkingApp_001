package com.cs743.uwmparkingfinder.GPSLocation;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class ProviderLocationTracker implements LocationListener, LocationTracker {

    // The minimum distance to change Updates in meters
    private static final long MIN_UPDATE_DISTANCE = 10;

    // The minimum time between updates in milliseconds
    private static final long MIN_UPDATE_TIME = 1000 * 1;

    private LocationManager lm;

    public enum ProviderType{
        NETWORK,
        GPS
    }
    private String provider;

    private Location lastLocation;
    private long lastTime;

    private boolean isRunning;

    private LocationUpdateListener listener;

    public ProviderLocationTracker(Context context, ProviderType type) {
        lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if(type == ProviderType.NETWORK){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else{
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    public void start(){
        if(isRunning){
            //Already running, do nothing
            return;
        }

        //The provider is on, so start getting updates.  Update current location
        isRunning = true;
        try{
            lm.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
        }catch (SecurityException e){
            //do something
            e.printStackTrace();
        }
        lastLocation = null;
        lastTime = 0;
        return;
    }

    public void start(LocationUpdateListener update) {
        start();
        listener = update;

    }


    public void stop(){
        if(isRunning){
            try{
                lm.removeUpdates(this);
            }catch (SecurityException e){
                //do something
                e.printStackTrace();
            }
            isRunning = false;
            listener = null;
        }
    }

    public boolean hasLocation(){
        if(lastLocation == null){
            return false;
        }
        if(System.currentTimeMillis() - lastTime > 5 * 60 * 1000){
            System.out.println(System.currentTimeMillis());
            System.out.println(lastTime);
            System.out.println("STALE");
            return false; //stale
        }
        return true;
    }

    public boolean hasPossiblyStaleLocation(){
        if(lastLocation != null){
            return true;
        }
        try{
            return lm.getLastKnownLocation(provider)!= null;
        }catch (SecurityException e){
            //do something
            return true;
        }

    }

    public Location getLocation(){
        if(lastLocation == null){
            return null;
        }
        if(System.currentTimeMillis() - lastTime > 5 * MIN_UPDATE_TIME){
            return null; //stale
        }
        return lastLocation;
    }

    public Location getPossiblyStaleLocation(){
        if(lastLocation != null){
            return lastLocation;
        }
        try {
            return lm.getLastKnownLocation(provider);
        }catch (SecurityException e){
            //do something
            return null;
        }
    }

    public void onLocationChanged(Location newLoc) {
        long now = System.currentTimeMillis();
        if(listener != null){
            listener.onUpdate(lastLocation, lastTime, newLoc, now);
        }
        lastLocation = newLoc;
        lastTime = now;
    }

    public void onProviderDisabled(String arg0) {

    }

    public void onProviderEnabled(String arg0) {

    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }
}