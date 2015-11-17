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
public class PhotoService extends IntentService implements SensorEventListener {

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


    //Camera variables
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Camera.Parameters parameters;

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            //decode the data obtained by the camera into a Bitmap

            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream("/sdcard/Image2.jpg");
                outStream.write(data);
                outStream.close();
            } catch (FileNotFoundException e) {
                Log.d("meng", e.getMessage());
            } catch (IOException e) {
                Log.d("meng", e.getMessage());
            }

        }
    };

    public PhotoService() {
        super("PhotoService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
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
        mCamera = Camera.open();
        SurfaceView sv = new SurfaceView(getApplicationContext());


        try {
            mCamera.setPreviewDisplay(sv.getHolder());
            parameters = mCamera.getParameters();

            //set camera parameters
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            mCamera.takePicture(null, null, mCall);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        //Get a surface
        sHolder = sv.getHolder();
        //tells Android that this surface will have its data constantly replaced
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
