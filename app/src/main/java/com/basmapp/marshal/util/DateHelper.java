package com.basmapp.marshal.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    public static final String DATE_FORMAT = "dd/MM/yy";

    public static Date stringToDate(String string) {
        if (string != null) {
            DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            try {
                return format.parse(string);
            } catch (ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String dateToString(Date date) {
        if (date != null) {
            try {
                DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
                return dateFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else return "";
    }

    public static String getTimeStringFromDate(Date date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            String fullDateTime[] = dateFormat.format(date).split(" ");
            return fullDateTime[1];
        } else return "";
    }

    public static String getDateStringFromDate(Date date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            String fullDateTime[] = dateFormat.format(date).split(" ");
            return fullDateTime[0];
        } else return "";
    }
}
