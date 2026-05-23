package com.sinu.molla;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class UninstallabilityChecker {
    // User app, uninstallable
    public static final int UNINSTALLABILITY_UNINSTALLABLE = 0;
    // System app with update installed - only the update can be uninstalled
    public static final int UNINSTALLABILITY_UPDATE_UNINSTALLABLE = 1;
    // System app, not uninstallable
    public static final int UNINSTALLABILITY_NOT_UNINSTALLABLE = 2;

    public static int checkUninstallability(Context context, String packageName) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(packageName, 0);
            boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isUpdatedSystem = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

            if (!isSystem) return UNINSTALLABILITY_UNINSTALLABLE;
            if (isUpdatedSystem) return UNINSTALLABILITY_UPDATE_UNINSTALLABLE;
            return UNINSTALLABILITY_NOT_UNINSTALLABLE;

        } catch (PackageManager.NameNotFoundException e) {
            return UNINSTALLABILITY_NOT_UNINSTALLABLE;
        }
    }
}
