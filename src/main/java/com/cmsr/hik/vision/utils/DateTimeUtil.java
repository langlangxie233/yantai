package com.cmsr.hik.vision.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {

    public static String dateFormat(String dateString) {
        String formDateString = dateString;
        if (dateString.contains(".")) {
            formDateString = dateString.substring(0, dateString.indexOf("."));
        }

        // 创建一个DateTimeFormatter，用于解析原始日期字符串
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 解析原始日期字符串为LocalDateTime对象
        LocalDateTime localDateTime = LocalDateTime.parse(formDateString, parser);

        // 转换为ZonedDateTime，并指定时区（这里使用Asia/Shanghai，即东八区）
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));

        // 创建一个DateTimeFormatter，用于格式化输出日期为所需的格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        // 格式化ZonedDateTime对象为所需的字符串格式
        return formatter.format(zonedDateTime);
    }

    public static String getYesterdayBeginTime() {
        LocalDateTime nowTime = LocalDateTime.now();// 减去一天，得到昨天的日期和时间
        LocalDateTime yesterday = nowTime.minus(1, ChronoUnit.DAYS);

        // 设置时间为00:00:00
        return yesterday.withHour(0).withMinute(0).withSecond(01).withNano(0).toString().replace("T", " ");
    }

    public static String getSecondsBefore(Integer seconds) {
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime secondsBefore = nowTime.minus(seconds, ChronoUnit.SECONDS);
        return secondsBefore.withNano(0).toString().replace("T", " ");
    }

    public static String getMinutesBefore(Integer minutes) {
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime minutesBefore = nowTime.minus(minutes, ChronoUnit.MINUTES);
        return minutesBefore.withNano(0).toString().replace("T", " ");
    }

    public static String getDaysBefore(Integer days) {
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime minutesBefore = nowTime.minus(days, ChronoUnit.DAYS);
        return minutesBefore.withNano(0).toString().replace("T", " ");
    }

    public static String getHoursBefore(Integer hours) {
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime hoursBefore = nowTime.minus(hours, ChronoUnit.HOURS);
        return hoursBefore.withNano(0).toString().replace("T", " ");
    }

}
