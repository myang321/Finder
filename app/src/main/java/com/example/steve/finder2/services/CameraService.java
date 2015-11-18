package com.example.steve.finder2.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.steve.finder2.constants.Const;
import com.example.steve.finder2.delegates.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class CameraService extends Service {
    //Camera variables
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Camera.Parameters parameters;
    private SurfaceView sv;
    private String username;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        username = intent.getStringExtra(Const.SHARED_PREF_USERNAME);
        releaseCameraAndPreview();
        mCamera = Camera.open(1);
        sv = new SurfaceView(getApplicationContext());


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

    public String getTimestamp() {
        String date = dateToString(new Date());
        date = date.replace(" ", "_");
        return date;
    }

    public String dateToString(Date date) {
        Timestamp ts = new Timestamp(date.getTime());
        return ts.toString();
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            //decode the data obtained by the camera into a Bitmap

            FileOutputStream outStream = null;
            String filepath = "/sdcard/" + getTimestamp() + ".jpg";
            try {
                outStream = new FileOutputStream(filepath);
                outStream.write(data);
                outStream.close();
            } catch (FileNotFoundException e) {
                Log.d("CAMERA", e.getMessage());
            } catch (IOException e) {
                Log.d("CAMERA", e.getMessage());
            }
            File image = new File(filepath);
            sendPhoto(image, "iphone7");
            releaseCameraAndPreview();

        }
    };

    private void sendPhoto(File image, String device_name) {
        RequestParams params = new RequestParams();
        try {
            params.put("image", image);
        } catch (FileNotFoundException e) {
        }
        params.put("username", username);
        params.put("device_name", device_name);
        // real time stamp cause problem
        params.put("timestamp", "123");

        // send request
        String url = "http://finderserver.sinaapp.com/finder_server/image_upload";
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                // handle success response
                Log.d("meng", "image sent successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // handle failure response
                Log.d("meng", "image sent failed");
            }
        });
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
