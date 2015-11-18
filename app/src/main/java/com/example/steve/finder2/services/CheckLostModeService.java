package com.example.steve.finder2.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.example.steve.finder2.constants.Const;
import com.example.steve.finder2.delegates.SharedPreferenceDelegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckLostModeService extends IntentService {
    private SharedPreferenceDelegate sharedPreferenceDelegate = null;

    public CheckLostModeService() {
        super("CheckLostModeService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferenceDelegate = new SharedPreferenceDelegate(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (getUsername() == null)
            return;
        if (isLostModeOn()) {
            startLostMode();
        } else {
            while (!isLostModeOn()) {
                boolean result = queryServer(getUsername());
                if (result) {
                    startLostMode();
                    break;
                }
                // sleep
                try {
                    Log.d("meng", "sleep in check lost mode");
                    Thread.sleep(Const.CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getUsername() {
        String name = sharedPreferenceDelegate.getSharedPrefsString(Const.SHARED_PREF_USERNAME);
        if (name == null || name.isEmpty())
            return null;
        return name;
    }

    private boolean isLostModeOn() {
        String mode = sharedPreferenceDelegate.getSharedPrefsString(Const.SHARED_PREF_PHONE_STATUS);
        return mode.equals(Const.PHONE_STATUS_LOST);
    }

    public void startLostMode() {
        Log.d("meng", "start lost mode");
        sharedPreferenceDelegate.setSharedPrefsString(Const.SHARED_PREF_PHONE_STATUS, Const.PHONE_STATUS_LOST);
        // start ReportService
        Intent intent1 = new Intent(this, ReportService.class);
        intent1.putExtra(Const.SHARED_PREF_USERNAME, getUsername());
        startService(intent1);
        // start SensorService
        Intent intent2 = new Intent(this, SensorService.class);
        intent2.putExtra(Const.SHARED_PREF_USERNAME, getUsername());
        startService(intent2);
    }

    private boolean queryServer(String username) {
        String uriBase = "http://finderserver.sinaapp.com/finder_server/check_mode?username=%s";
        String struri = String.format(uriBase, username);
        Log.d("meng", struri);
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String authJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            URL url = new URL(struri);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            //urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                authJsonStr = "input stream null";
                Log.d("meng", authJsonStr);
                return false;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                authJsonStr = "buffer len=0 ";
                Log.d("meng", authJsonStr);
                return false;
            }
            authJsonStr = buffer.toString();
            Log.d("meng", authJsonStr);
        } catch (IOException e) {
            Log.e("IOException", "Error ", e);
            e.printStackTrace();
            authJsonStr = "IO error";
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        boolean result = authJsonStr.trim().equals("{\"mode\": \"on\"}");
        return result;
    }


}
