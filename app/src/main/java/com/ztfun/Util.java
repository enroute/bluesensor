package com.ztfun;

public class Util {
//    private static String[] BYTE2HEX_STRING = new String[] {
//            "00", "01", "02", "03", "04", "05", "06", "07",
//            "08", "09", "0A",
//    };

    public static String byte2Hex(byte b) {
        // return BYTE2HEX_STRING[b & 0xFF];
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2){
            hex = "0" + hex;
        }
        return hex.toUpperCase();
    }
}
