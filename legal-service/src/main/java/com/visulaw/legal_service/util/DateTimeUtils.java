package com.visulaw.legal_service.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@UtilityClass
public class DateTimeUtils {

    public static Instant stringToInstant(String timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    public static String timeStampToString(Timestamp timestamp) {
        try {
            Instant timeInInstant = timestamp.toInstant();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy hh:mm a");
            LocalDateTime localDateTime = LocalDateTime.ofInstant(timeInInstant, ZoneId.systemDefault());
            return formatter.format(localDateTime);
        } catch (Exception e) {
            log.warn("Could not parse Timestamp {} to a readable format", timestamp);
        }
         return null;
    }
}
