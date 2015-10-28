package com.example.steve.finder2.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.steve.finder2.constants.Const;
import com.example.steve.finder2.delegates.SharedPreferenceDelegate;
import com.example.steve.finder2.delegates.Utils;
import com.example.steve.finder2.location.MyLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReportService extends IntentService {

    //    private SharedPreferenceDelegate sharedPreferenceDelegate = null;
    private String username;

    public ReportService() {
        super("ReportService");
//        sharedPreferenceDelegate = new SharedPreferenceDelegate(this);
        Log.d("meng", "service constructor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("meng", "service on handle intent");
        username = intent.getStringExtra(Const.SHARED_PREF_USERNAME);
        while (true) {
            MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    //Got the location!
                    Log.d("meng", "location x=" + location.getLatitude() + " y=" + location.getAltitude());
                    Log.d("meng", "accuracy " + location.getAccuracy());
                    sendReport(location.getLatitude(), location.getAltitude());
                }
            };
            MyLocation myLocation = new MyLocation();
            myLocation.getLocation(this, locationResult);

            try {
                Thread.sleep(Const.REPORT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendReport(double loc_x, double loc_y) {
        String date = Utils.getTimestamp();
        String uriBase1 = "http://finderserver.sinaapp.com/finder_server/upload_report_mobile?";
        String uriBase2 = "username=%s&timestamp=%s&location_x=%.6f&location_y=%.6f&ip_addr=192.168.1.1&wifi_name=kandedan&device_name=iphone";
        String struri = String.format(uriBase1 + uriBase2, username, date, loc_x, loc_y);
        Log.d("meng", struri);
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            URL url = new URL(struri);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

}
