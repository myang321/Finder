package com.example.steve.finder2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.steve.finder2.services.CheckLostModeService;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent(context, CheckLostModeService.class);
        context.startService(intent2);
    }
}
