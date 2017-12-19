package com.example.jyn.remotemeeting.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JYN on 2017-12-05.
 */

public class Get_currentTime {
    //자바 포맷: yyyyMMddHHmmssSSS,  php 포맷: Y-m-d H:i:s
    public static String get_full() {
        SimpleDateFormat format_for_save = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        long time_mil = System.currentTimeMillis();
        Date date_start_broadCast = new Date(time_mil);
        String result = format_for_save.format(date_start_broadCast);
        return result;
    }

    public static String get_until_day() {
        SimpleDateFormat format_for_save = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        long time_mil = System.currentTimeMillis();
        Date date_start_broadCast = new Date(time_mil);
        String result = format_for_save.format(date_start_broadCast);
        return result;
    }

    public static String get_only_time() {
        SimpleDateFormat format_for_save = new SimpleDateFormat("HH:mm", Locale.KOREA);
        long time_mil = System.currentTimeMillis();
        Date date_start_broadCast = new Date(time_mil);
        String result = format_for_save.format(date_start_broadCast);
        return result;
    }
}
