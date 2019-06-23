package com.nish;


import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;

public class TimeUtils {
    private static final String TIME_FORMAT = "hh:mm a";

    //TODO add any member variables required for pretty output

    public String convertTime(ZoneId source, ZoneId destination, ZonedDateTime time){
        ZonedDateTime destTime = time.withZoneSameInstant(destination) ; // Same moment adjusted into another time zone.
        String formatted = DateTimeFormatter.ofPattern(TIME_FORMAT).format(destTime);
        return formatted;
    }
}
