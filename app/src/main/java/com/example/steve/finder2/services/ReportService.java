package com.example.steve.finder2.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
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
    private double loc_x = 0;
    private double loc_y = 0;
    private SharedPreferenceDelegate sharedPreferenceDelegate = null;

    public ReportService() {
        super("ReportService");
//        sharedPreferenceDelegate = new SharedPreferenceDelegate(this);
        Log.d("meng", "service constructor");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (sharedPreferenceDelegate == null)
            sharedPreferenceDelegate = new SharedPreferenceDelegate(this);
        Log.d("meng", "service on handle intent");
        username = intent.getStringExtra(Const.SHARED_PREF_USERNAME);
        startReport();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("meng1", "onDestroy report service");
    }

    private boolean isLostModeOn() {
        String mode = sharedPreferenceDelegate.getSharedPrefsString(Const.SHARED_PREF_PHONE_STATUS);
        return mode.equals(Const.PHONE_STATUS_LOST);
    }

    private void startReport() {
        while (isLostModeOn()) {
            Log.d("meng", "in report service loop");
            MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    //Got the location!
                    Log.d("meng", "got location x=" + location.getLatitude() + " y=" + location.getAltitude());
                    Log.d("meng", "accuracy " + location.getAccuracy());
                    loc_x = location.getLatitude();
                    loc_y = location.getLongitude();
                }
            };
            MyLocation myLocation = new MyLocation();
            myLocation.getLocation(this, locationResult);
            float battery = getBatteryLevel();
            int sleepTime = Const.REPORT_INTERVAL;
            if (battery < 30 && battery > 20)
                sleepTime *= 3;
            else if (battery < 20 && battery > 10)
                sleepTime *= 12;
            else if (battery < 10)
                sleepTime *= 60;
            Log.d("meng", "battery :" + battery);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendReport();
        }
    }

    private void sendReport() {
        String date = Utils.getTimestamp();
        String uriBase1 = "http://finderserver.sinaapp.com/finder_server/upload_report_mobile?";
        String uriBase2 = "username=%s&timestamp=%s&location_x=%.6f&location_y=%.6f&ip_addr=%s&wifi_name=%s&device_name=%s";
        String ip = Utils.getIPAddress(true);
        String wifi = getWifiName();
        String device_name = Utils.getDeviceName();
        String struri = String.format(uriBase1 + uriBase2, username, date, loc_x, loc_y, ip, wifi, device_name);
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

    public String getWifiName() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getSSID();
    }

    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return ((float) level / (float) scale) * 100.0f;
    }

}
