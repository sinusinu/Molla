// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;

public class AppItemIcon {
    public enum IconType { LEANBACK, NORMAL }

    public IconType type;
    public Drawable drawable;

    public AppItemIcon(IconType type, Drawable drawable) {
        this.type = type;
        this.drawable = drawable;
    }

    public static AppItemIcon getAppItemIcon(MollaApplication context, AppItem appItem) {
        if (appItem.isCustomItem) {
            File customBanner = new File(context.getFilesDir(), appItem.customItemIdentifier + ".webp");
            if (customBanner.exists()) {
                var customBannerDrawable = Drawable.createFromPath(customBanner.getAbsolutePath());
                return new AppItemIcon(IconType.LEANBACK, customBannerDrawable);
            }
        }

        AppItemIcon ci;
        ci = context.getCachedAppIcon(appItem.packageName);
        if (ci != null) {
            return ci;
        } else {
            Drawable appBanner;
            Drawable appIcon;
            AppItemIcon ret;
            try {
                appBanner = context.getPackageManager().getApplicationBanner(appItem.packageName);
                if (appBanner == null) {
                    appBanner = context.getPackageManager().getActivityBanner(appItem.intent);
                    if (appBanner == null) {
                        appIcon = context.getPackageManager().getApplicationIcon(appItem.packageName);
                        ret = new AppItemIcon(AppItemIcon.IconType.NORMAL, appIcon);
                        context.cacheAppIcon(appItem.packageName, ret);
                    } else {
                        ret = new AppItemIcon(AppItemIcon.IconType.LEANBACK, appBanner);
                        context.cacheAppIcon(appItem.packageName, ret);
                    }
                } else {
                    ret = new AppItemIcon(AppItemIcon.IconType.LEANBACK, appBanner);
                    context.cacheAppIcon(appItem.packageName, ret);
                }
            } catch (PackageManager.NameNotFoundException e) {
                ret = new AppItemIcon(AppItemIcon.IconType.NORMAL, null);
                context.cacheAppIcon(appItem.packageName, ret);
            }
            return ret;
        }
    }
}
