package com.example.steve.finder2.delegates;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Steve on 10/26/2015.
 */
public class Utils {
    public static String getTimestamp() {
        String date = dateToString(new Date());
        date = date.replace(" ", "_");
        return date;
    }

    public static String dateToString(Date date) {
        Timestamp ts = new Timestamp(date.getTime());
        return ts.toString();
    }
}
