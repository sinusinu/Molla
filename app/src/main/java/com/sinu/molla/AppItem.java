// Copyright 2022 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppItem {
    public String packageName;
    public String activityName;
    public String displayName;
    public Intent intent;

    private AppItem(String packageName, String activityName, String displayName, Intent intent) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.displayName = displayName;
        this.intent = intent;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof AppItem)) return false;
        AppItem other = (AppItem)obj;
        return packageName.equals(other.packageName) && activityName.equals(other.activityName);
    }

    public static int compare(AppItem o1, AppItem o2) {
        if (o1.packageName.equals(o2.packageName)) return o1.activityName.compareTo(o2.activityName);
        return o1.packageName.compareTo(o2.packageName);
    }

    public static int compareByDisplayName(AppItem o1, AppItem o2) {
        return o1.displayName.compareTo(o2.displayName);
    }

    public static ArrayList<AppItem> fetchApps(Context context) {
        ArrayList<AppItem> ret = new ArrayList<>();

        PackageManager pm = context.getPackageManager();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
        for(ResolveInfo ri : allApps) {
            if (ri.activityInfo.packageName.equals("com.sinu.molla")) continue;
            Intent intentForThisActivity = new Intent();
            intentForThisActivity.setClassName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name);
            AppItem n = new AppItem(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name, ri.activityInfo.loadLabel(pm).toString(), intentForThisActivity);
            ret.add(n);
        }

        return ret;
    }

    public static ArrayList<AppItem> fetchTvApps(Context context) {
        ArrayList<AppItem> ret = new ArrayList<>();

        PackageManager pm = context.getPackageManager();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

        List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
        for(ResolveInfo ri : allApps) {
            if (ri.activityInfo.packageName.equals("com.sinu.molla")) continue;
            Intent intentForThisActivity = new Intent();
            intentForThisActivity.setClassName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name);
            AppItem n = new AppItem(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name, ri.loadLabel(pm).toString(), intentForThisActivity);
            ret.add(n);
        }

        return ret;
    }

    public static ArrayList<AppItem> fetchAllApps(Context context) {
        ArrayList<AppItem> ret = new ArrayList<>();

        ArrayList<AppItem> apps = fetchApps(context);
        ArrayList<AppItem> tvApps = fetchTvApps(context);

        SharedPreferences pref = context.getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);
        if (pref.getInt("hide_non_tv", 0) == 0) {
            HashSet<String> addedApps = new HashSet<>();

            for (AppItem item : tvApps) {
                addedApps.add(item.packageName);
                ret.add(item);
            }

            for (AppItem item : apps) if (!addedApps.contains(item.packageName)) ret.add(item);
        } else {
            ret.addAll(tvApps);
        }

        return ret;
    }

    public static ArrayList<AppItem> fetchListOfApps(Context context, List<String> packageNames) {
        ArrayList<AppItem> ret = new ArrayList<>();

        ArrayList<AppItem> allApps = fetchAllApps(context);

        ArrayList<String> pnames = new ArrayList<>();

        for (int i = 0; i < allApps.size(); i++) {
            pnames.add(allApps.get(i).packageName);
        }

        for (int i = 0; i < packageNames.size(); i++) {
            int idx = pnames.indexOf(packageNames.get(i));
            if (idx != -1) ret.add(allApps.get(idx));
        }

        return ret;
    }
}
