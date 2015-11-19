package com.example.steve.finder2.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.steve.finder2.constants.Const;
import com.example.steve.finder2.delegates.SharedPreferenceDelegate;
import com.example.steve.finder2.delegates.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SensorService extends IntentService implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private static final int SHAKE_THRESHOLD_LOW = 10;
    private static final int SHAKE_THRESHOLD_HIGH = 200;
    private static final int MOVE_COUNT_THRESHOLD = 20;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private int move_cnt = 0;
    private int still_cnt = 0;
    private boolean isPicTaken = false;
    private String username;
    private SharedPreferenceDelegate sharedPreferenceDelegate = null;

    public SensorService() {
        super("SensorService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (sharedPreferenceDelegate == null)
            sharedPreferenceDelegate = new SharedPreferenceDelegate(this);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        username = intent.getStringExtra(Const.SHARED_PREF_USERNAME);
        Log.d("meng1", "sensor service onHandleIntent exit");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("meng1", "onDestroy sensor service");
    }

    private boolean isLostModeOn() {
        String mode = sharedPreferenceDelegate.getSharedPrefsString(Const.SHARED_PREF_PHONE_STATUS);
//        Log.d("meng1", "lost mode " + mode);
        return mode.equals(Const.PHONE_STATUS_LOST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isLostModeOn()) {
            Log.d("meng1", "unregisterListener sensor");
            senSensorManager.unregisterListener(this);
            return;
        }

        if (!Utils.isScreenOn(this))
            return;
        Sensor mySensor = event.sensor;

        float x = 0;
        float y = 0;
        float z = 0;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
        }
        long curTime = System.currentTimeMillis();
        // wait for some time before sending the next one
        if (isPicTaken && (curTime - lastUpdate) < Const.PICTURE_SLEEP)
            return;
        else
            isPicTaken = false;
        // check for every 100ms
        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            // calculate movement
            float speed = (Math.abs(x - last_x) + Math.abs(y - last_y) + Math.abs(z - last_z)) / diffTime * 10000;
//            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

            if (SHAKE_THRESHOLD_LOW < speed && speed < SHAKE_THRESHOLD_HIGH) {
                move_cnt++;
            } else
                still_cnt++;
            if (still_cnt > 3) {
                move_cnt = 0;
                still_cnt = 0;
            }

            Log.d("men", "move cnt:" + move_cnt + " speed: " + speed);
            // take picture of user
            if (move_cnt > MOVE_COUNT_THRESHOLD) {
                Log.d("meng", "shake detected ***************");
                move_cnt = 0;
                still_cnt = 0;
                isPicTaken = true;
                takePic();
            }
            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    private void takePic() {
        Intent intent = new Intent(this, CameraService.class);
        intent.putExtra(Const.SHARED_PREF_USERNAME, username);
        startService(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
