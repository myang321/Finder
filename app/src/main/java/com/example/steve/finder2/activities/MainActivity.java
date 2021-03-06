package com.example.steve.finder2.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steve.finder2.R;
import com.example.steve.finder2.constants.Const;
import com.example.steve.finder2.delegates.SharedPreferenceDelegate;
import com.example.steve.finder2.services.CheckLostModeService;
import com.example.steve.finder2.services.ReportService;
import com.example.steve.finder2.services.SensorService;

public class MainActivity extends Activity {
    private SharedPreferenceDelegate sharedPreferenceDelegate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferenceDelegate = new SharedPreferenceDelegate(this);
        setText();
        Log.d("meng1", "main activity oncreate");
        if (isLostModeOn())
            startLostMode(null);
        else
            checkLostMode();
    }

    private void setText() {
        TextView textView = (TextView) findViewById(R.id.textView1);
        if (isLostModeOn())
            textView.setText("lost mode on");
        else
            textView.setText("lost mode off");
    }

    private boolean isLostModeOn() {
        String mode = sharedPreferenceDelegate.getSharedPrefsString(Const.SHARED_PREF_PHONE_STATUS);
        Log.d("meng1", "lost mode " + mode);
        return mode.equals(Const.PHONE_STATUS_LOST);
    }

    private boolean verifyPassword(String pass) {
        String savedPass = sharedPreferenceDelegate.getSharedPrefsString(Const.SHARED_PREF_PASSWORD);
        if (pass == null || pass.isEmpty())
            return false;
        return savedPass.equals(pass);
    }

    private void logout() {
        sharedPreferenceDelegate.setSharedPrefsString(Const.SHARED_PREF_USERNAME, "");
        sharedPreferenceDelegate.setSharedPrefsString(Const.SHARED_PREF_PASSWORD, "");
        stopLostMode();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    public void showLogoutDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter password");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pass = input.getText().toString();
                if (verifyPassword(pass)) {
                    logout();
                } else {
                    Toast.makeText(getApplicationContext(), "password wrong", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void startLostMode(View view) {
        setText();
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

//        Intent intent3 = new Intent(this, CameraService.class);
//        startService(intent3);
    }

    public void showStopLostModeDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter password");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pass = input.getText().toString();
                if (verifyPassword(pass)) {
                    stopLostMode();
                } else {
                    Toast.makeText(getApplicationContext(), "password wrong", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void stopLostMode() {
        setText();
        sharedPreferenceDelegate.setSharedPrefsString(Const.SHARED_PREF_PHONE_STATUS, Const.PHONE_STATUS_NOT_LOST);
        Log.d("meng1", "stopService ReportService");
        stopService(new Intent(this, ReportService.class));
        Log.d("meng1", "stopService SensorService");
        stopService(new Intent(this, SensorService.class));
    }

    private String getUsername() {
        return sharedPreferenceDelegate.getSharedPrefsString(Const.SHARED_PREF_USERNAME);
    }

    private void checkLostMode() {
        Intent intent2 = new Intent(this, CheckLostModeService.class);
        startService(intent2);
    }


}
