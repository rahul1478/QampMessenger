package com.qamp.app.CustomClasses;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatConverter {

    public static String convertToAmPm(String timestamp) {
        try {
            // Define the input and output date formats
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");

            // Parse the input timestamp
            Date date = inputFormat.parse(timestamp);

            // Format the date in AM/PM format
            String formattedTime = outputFormat.format(date);

            return formattedTime;
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle the parsing error
        }
    }
}
