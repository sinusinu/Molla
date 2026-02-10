// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.io.File;

public class AppItemIcon {
    public enum IconType { LEANBACK, NORMAL }

    public IconType type;
    public Drawable drawable;

    public AppItemIcon(IconType type, Drawable drawable) {
        this.type = type;
        this.drawable = drawable;
    }

    public static AppItemIcon getAppItemIcon(MollaApplication application, AppItem appItem) {
        return getAppItemIcon(application, appItem, true);
    }

    public static AppItemIcon getAppItemIcon(MollaApplication application, AppItem appItem, boolean loadCustom) {
        if (loadCustom && appItem.isCustomItem) {
            File customBanner = new File(application.getFilesDir(), appItem.customItemIdentifier + ".webp");
            if (customBanner.exists()) {
                var customBannerDrawable = Drawable.createFromPath(customBanner.getAbsolutePath());
                return new AppItemIcon(IconType.LEANBACK, customBannerDrawable);
            }
        }

        AppItemIcon ci;
        ci = application.getCachedAppIcon(appItem.packageName);
        if (ci != null) {
            return ci;
        } else {
            Drawable appBanner;
            Drawable appIcon;
            AppItemIcon ret;
            try {
                appBanner = application.getPackageManager().getApplicationBanner(appItem.packageName);
                if (appBanner == null) {
                    appBanner = application.getPackageManager().getActivityBanner(appItem.intent);
                    if (appBanner == null) {
                        appIcon = application.getPackageManager().getApplicationIcon(appItem.packageName);
                        ret = new AppItemIcon(AppItemIcon.IconType.NORMAL, appIcon);
                        application.cacheAppIcon(appItem.packageName, ret);
                    } else {
                        ret = new AppItemIcon(AppItemIcon.IconType.LEANBACK, appBanner);
                        application.cacheAppIcon(appItem.packageName, ret);
                    }
                } else {
                    ret = new AppItemIcon(AppItemIcon.IconType.LEANBACK, appBanner);
                    application.cacheAppIcon(appItem.packageName, ret);
                }
            } catch (PackageManager.NameNotFoundException e) {
                ret = new AppItemIcon(AppItemIcon.IconType.NORMAL, null);
                application.cacheAppIcon(appItem.packageName, ret);
            }
            return ret;
        }
    }
}
