package com.basmapp.marshal.util;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RootUtils {

    private final Context mContext;

    public RootUtils(Context context) {
        mContext = context;
    }

    private static final String[] knownRootManagementApps = {
            "com.noshufou.android.su", /* ChainsDD's Superuser */
            "eu.chainfire.supersu", /* Chainfire's SuperSU */
            "com.koushikdutta.superuser", /* Koush's Superuser */
            "com.genymotion.superuser", /* Genymotion's Superuser */
            "com.yellowes.su",
            "org.masteraxe.superuser"
    };

    private static final String[] knownRootCloakingApps = {
            "com.devadvance.rootcloak2", /* RootCloak */
            "com.devadvance.rootcloakplus", /* RootCloak Plus */
            "de.robv.android.xposed.installer", /* Xposed Installer */
            "com.saurik.substrate", /* Cydia Substrate */
            "com.amphoras.hidemyroot",
            "com.formyhm.hideroot"
    };

    public boolean isRooted() {
        boolean testKeys = detectTestKeys();
        boolean rootManagement = detectRootManagementApps();
        boolean rootCloaking = detectRootCloakingApps();
        boolean suBinary = checkForSuBinary();
        boolean busyBoxBinary = checkForBusyBoxBinary();
        boolean testSuExists = checkSuExists();
        return testKeys || rootManagement || rootCloaking || suBinary || busyBoxBinary || testSuExists;
    }

    private boolean detectTestKeys() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private boolean detectRootManagementApps() {
        return detectRootManagementApps(null);
    }

    private boolean detectRootManagementApps(String[] additionalRootManagementApps) {
        // Return true if Superuser.apk found on /system
        if (new File("/system/app/Superuser.apk").exists()) return true;
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
        return findBinary("su");
    }

    private boolean checkForBusyBoxBinary() {
        return (findBinary("busybox")) || (findBinary("toybox"));
    }

    /**
     * @param filename - check for existence of this binary
     * @return true if binary found
     */
    private boolean findBinary(String filename) {
        // searchPaths is a list of all PATH environment variables
        List<String> searchPaths = Arrays.asList(System.getenv("PATH").split(":"));
        for (String path : searchPaths) {
            if (!path.endsWith("/")) {
                path += "/";
            }
            String filePath = path + filename;
            if (new File(filePath).exists()) {
                // binary detected
                return true;
            }
        }
        return false;
    }

    /**
     * @param packages - check for existence of this package names
     * @return true if one of the apps is installed
     */
    private boolean isAnyPackageFromListInstalled(List<String> packages) {
        boolean result = false;

        PackageManager pm = mContext.getPackageManager();

        for (String packageName : packages) {
            try {
                // app detected
                pm.getPackageInfo(packageName, 0);
                result = true;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Another method to check for su, this attempts a 'which su'
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