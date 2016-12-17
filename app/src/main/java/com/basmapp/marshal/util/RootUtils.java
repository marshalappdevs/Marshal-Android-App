package com.basmapp.marshal.util;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RootUtils {

    private final Context mContext;

    public RootUtils(Context context) {
        mContext = context;
    }

    private static final String[] knownRootManagementApps = {
            "com.noshufou.android.su", /* ChainsDD's Superuser */
            "com.noshufou.android.su.elite", /* ChainsDD's Superuser Elite */
            "eu.chainfire.supersu", /* Chainfire's SuperSU */
            "com.koushikdutta.superuser", /* Koush's ClockworkMod Superuser */
            "com.thirdparty.superuser", /* Koush's OpenSource Superuser */
            "com.yellowes.su",
            "org.masteraxe.superuser",
    };

    private static final String[] knownRootCloakingApps = {
            "com.devadvance.rootcloak", /* RootCloak (Deprecated) */
            "com.devadvance.rootcloak2", /* RootCloak */
            "com.devadvance.rootcloakplus", /* RootCloak Plus */
            "de.robv.android.xposed.installer", /* Xposed Installer */
            "com.saurik.substrate", /* Cydia Substrate */
            "com.zachspong.temprootremovejb",
            "com.amphoras.hidemyroot",
            "com.formyhm.hideroot"
    };

    private static final String[] suPaths = {
            "/data/local/",
            "/data/local/bin/",
            "/data/local/xbin/",
            "/system/bin/",
            "/system/bin/.ext/",
            "/system/bin/failsafe/",
            "/system/xbin/",
            "/system/sd/xbin/",
            "/system/usr/we-need-root/",
            "/sbin/"
    };

    public boolean isRooted() {
        boolean testKeys = detectTestKeys();
        boolean rootManagement = detectRootManagementApps();
        boolean rootCloaking = detectRootCloakingApps();
        boolean suBinary = checkForSuBinary();
        boolean busyBoxBinary = checkForBusyBoxBinary();
        boolean dangerousProps = checkForDangerousProps();
        boolean testSuExists = checkSuExists();
        return testKeys || rootManagement || rootCloaking || suBinary || busyBoxBinary || dangerousProps || testSuExists;
    }

    private boolean detectTestKeys() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private boolean detectRootManagementApps() {
        return detectRootManagementApps(null);
    }

    private boolean detectRootManagementApps(String[] additionalRootManagementApps) {
        // Create a list of package names to iterate over from constants any others provided
        ArrayList<String> packages = new ArrayList<>();
        packages.addAll(Arrays.asList(knownRootManagementApps));
        if (additionalRootManagementApps != null && additionalRootManagementApps.length > 0) {
            packages.addAll(Arrays.asList(additionalRootManagementApps));
        }
        return isAnyPackageFromListInstalled(packages);
    }

    private boolean detectRootCloakingApps() {
        return detectRootCloakingApps(null);
    }

    private boolean detectRootCloakingApps(String[] additionalRootCloakingApps) {
        // Create a list of package names to iterate over from constants any others provided
        ArrayList<String> packages = new ArrayList<>();
        packages.addAll(Arrays.asList(knownRootCloakingApps));
        if (additionalRootCloakingApps != null && additionalRootCloakingApps.length > 0) {
            packages.addAll(Arrays.asList(additionalRootCloakingApps));
        }
        return isAnyPackageFromListInstalled(packages);
    }

    private boolean checkForSuBinary() {
        return checkForBinary("su");
    }

    private boolean checkForBusyBoxBinary() {
        return checkForBinary("busybox");
    }

    /**
     * @param filename - check for this existence of this file
     * @return true if found
     */
    private boolean checkForBinary(String filename) {
        boolean result = false;
        for (String path : suPaths) {
            String completePath = path + filename;
            File f = new File(completePath);
            boolean fileExists = f.exists();
            if (fileExists) {
                // binary detected
                result = true;
            }
        }
        return result;
    }

    /**
     * Using the PackageManager, check for a list of given package names.
     *
     * @return true if one of the apps it's installed
     */
    private boolean isAnyPackageFromListInstalled(List<String> packages) {
        boolean result = false;

        PackageManager pm = mContext.getPackageManager();

        for (String packageName : packages) {
            try {
                // Root management app detected
                pm.getPackageInfo(packageName, 0);
                result = true;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private String[] propsReader() {
        InputStream inputstream = null;
        try {
            inputstream = Runtime.getRuntime().exec("getprop").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If input steam is null, we can't read the file, so return null
        if (inputstream == null) {
            return null;
        }

        String propValue = new Scanner(inputstream).useDelimiter("\\A").next();

        return propValue.split("\n");
    }

    // Checks for system properties that are typical to test ROMs or custom ROMs
    private boolean checkForDangerousProps() {
        final Map<String, String> dangerousProps = new HashMap<>();
        dangerousProps.put("ro.debuggable", "1");
        dangerousProps.put("ro.secure", "0");

        boolean result = false;

        String[] lines = propsReader();
        if (lines != null) {
            for (String line : lines) {
                for (String key : dangerousProps.keySet()) {
                    if (line.contains(key)) {
                        String badValue = dangerousProps.get(key);
                        badValue = "[" + badValue + "]";
                        if (line.contains(badValue)) {
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * A variation on the checking for SU, this attempts a 'which su'
     *
     * @return true if su found
     */
    private boolean checkSuExists() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}