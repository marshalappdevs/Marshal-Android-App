package com.basmapp.marshal.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    public static String SHA(String toHash) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(toHash.getBytes("UTF-8"), 0, toHash.length());
            byte[] bytes = messageDigest.digest();
            return bytesToHex(bytes).toLowerCase();
        } catch( NoSuchAlgorithmException | UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
        return null;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[ bytes.length * 2 ];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
        }
        return new String(hexChars);
    }
}
