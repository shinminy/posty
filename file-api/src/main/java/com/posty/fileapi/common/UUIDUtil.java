package com.posty.fileapi.common;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UUIDUtil {

    private static final Pattern DASH_PATTERN = Pattern.compile("-");

    public static String getUUIDWithoutDash() {
        String uuid = UUID.randomUUID().toString();
        Matcher matcher = DASH_PATTERN.matcher(uuid);
        return matcher.replaceAll("");
    }
}
