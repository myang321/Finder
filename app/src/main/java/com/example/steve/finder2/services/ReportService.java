package com.example.steve.finder2.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.example.steve.finder2.constants.Const;

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


    public ReportService() {
        super("ReportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) {
            sendReport();
            try {
                Thread.sleep(Const.REPORT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendReport() {
        String uriBase = "http://finderserver.sinaapp.com/finder_server/upload_report_mobile?username=abc&timestamp=20150213&location_x=1233&location_y=3321&ip_addr=192.168.1.1&wifi_name=kandedan&device_name=iphone";
        String struri = String.format(uriBase);
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
