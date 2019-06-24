package com.nish;

import java.time.format.DateTimeFormatter;
import java.time.*;

class TimeUtils
{
    static String convertTime(ZoneId destination, ZonedDateTime time)
    {
        ZonedDateTime destTime = time.withZoneSameInstant(destination) ; // Same moment adjusted into another time zone.
        return DateTimeFormatter.ofPattern("hh:mm a").format(destTime);
    }
}
