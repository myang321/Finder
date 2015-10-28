package com.example.steve.finder2.delegates;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.steve.finder2.constants.Const;

/**
 * Created by Steve on 10/24/2015.
 */
public class SharedPreferenceDelegate {
    private SharedPreferences sharedPref = null;
    private Context context = null;

    // constructor
    public SharedPreferenceDelegate(Context context) {
        this.context = context;
        this.sharedPref = context.getSharedPreferences(Const.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    // set string
    public void setSharedPrefsString(String key, String value) {
        SharedPreferences.Editor editor = this.sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    // get string
    public String getSharedPrefsString(String key) {
        String value = this.sharedPref.getString(key, null);
        return value;
    }
}
