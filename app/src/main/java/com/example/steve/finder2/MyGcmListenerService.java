package com.example.steve.finder2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyGcmListenerService extends Service {
    public MyGcmListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
