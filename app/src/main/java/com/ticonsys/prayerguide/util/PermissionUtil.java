package com.ticonsys.prayerguide.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
public abstract class PermissionUtil {

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the Activity has access to all given permissions.
     * Always returns true on platforms below M.
     *
     * @see Activity#checkSelfPermission(String)
     */
    public static boolean hasSelfPermission(Activity activity, String[] permissions) {
        // Below Android M all permissions are granted at install time and are already available.
        if (!isMNC()) {
            return true;
        }

        // Verify that all required permissions have been granted
        for (String permission : permissions) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the Activity has access to a given permission.
     * Always returns true on platforms below M.
     *
     * @see Activity#checkSelfPermission(String)
     */
    public static boolean hasSelfPermission(Activity activity, String permission) {
        // Below Android M all permissions are granted at install time and are already available.
        if (!isMNC()) {
            return true;
        }
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isMNC() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

}
