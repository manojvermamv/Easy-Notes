package com.anubhav.takeanote.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    private static final String TAG = DateTimeUtils.class.getSimpleName();

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy - hh:mm:ss aa", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static boolean isTimeBetweenTwoTime(String startTime, String endTime, String time) throws ParseException {
        return isTimeBetweenTwoTime(startTime, endTime, time, "HH:mm");
    }

    public static boolean isTimeBetweenTwoTime(String startTime, String endTime, String time, String pattern) throws ParseException {
        //all times are from java.util.Date

        String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9])$";
        String regSeconds = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
        if (startTime.matches(reg) && endTime.matches(reg) && time.matches(reg)) {
            boolean valid = false;
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());

            //Start Time
            Date inTime = sdf.parse(startTime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(inTime);

            //Check Time
            Date checkTime = sdf.parse(time);
            Calendar calendar3 = Calendar.getInstance();
            calendar3.setTime(checkTime);

            //End Time
            Date finTime = sdf.parse(endTime);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(finTime);

            if (endTime.compareTo(startTime) < 0) {
                calendar2.add(Calendar.DATE, 1);
                calendar3.add(Calendar.DATE, 1);
            }

            Date actualTime = calendar3.getTime();
            if ((actualTime.after(calendar1.getTime()) ||
                    actualTime.compareTo(calendar1.getTime()) == 0) &&
                    actualTime.before(calendar2.getTime())) {
                valid = true;
            }
            return valid;
        } else {
            throw new IllegalArgumentException("Not a valid time, expecting format is " + pattern);
        }
    }

    public static boolean isTimeGreater(String time, String anotherTime) throws ParseException {
        return isTimeGreater(time, anotherTime, "HH:mm");
    }

    public static boolean isTimeGreater(String time, String anotherTime, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());

        Date date = sdf.parse(time);
        Date anotherDate = sdf.parse(anotherTime);

        Log.e(TAG, "Date: " + date);
        Log.e(TAG, "Another Date: " + anotherDate);

        //compare > 0, if date1 is greater than date2
        //compare = 0, if date1 is equal to date2
        //compare < 0, if date1 is smaller than date2

        if (anotherDate.compareTo(date) > 0) {
            Log.e(TAG, "Another Time(" + anotherTime + ") is Greater than Time(" + time + ")");
            return true;
        } else {
            Log.e(TAG, "Another Time(" + anotherTime + ") is Less than Time(" + time + ")");
            return false;
        }
    }

}