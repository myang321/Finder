package com.example.steve.finder2.gcm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyInstanceIDListenerService extends Service {
    public MyInstanceIDListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
