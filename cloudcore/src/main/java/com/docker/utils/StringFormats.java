package com.docker.utils;

public class StringFormats {
    public static String toCamelCase(String[] strs) {
        StringBuffer str = new StringBuffer();
        for (String chars : strs) {
            str.append(chars.substring(0, 1).toUpperCase() + chars.substring(1));
        }
        String cam = str.toString();
        return cam.substring(0, 1).toLowerCase() + cam.substring(1);
    }
}
