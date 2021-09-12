package com.example.findaccesspoint;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import static android.content.Context.LOCATION_SERVICE;
import android.os.Bundle;
import android.util.Log;

public class GPSlocation implements LocationListener
{
    Context context;
    public GPSlocation(Context ct)
    {
        super();
        this.context = ct;
    }

    public Location getLocation() // Get GPS location Info
    {   // Check fine location Permission is enabled else return null value
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e("finelocation","Access Fine Location Permission Error");
            return null;
        }
        try //Check GPS is enabled then get the location info
        {
            LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled)
            {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,10,this);//need to parameterize
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return location;
            }else
                {
                Log.e("gps","GPS Not Enabled Error");
                }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
