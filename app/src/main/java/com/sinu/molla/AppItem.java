// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AppItem {
    // for standard items (also used in custom items)
    public String packageName;
    public String activityName;
    public String displayName;
    public Intent intent;

    // for custom items (null on standard items)
    public boolean isCustomItem = false;
    public String customItemIdentifier;
    public String customItemDisplayName;
    public String customItemActivityName;
    public ArrayList<AppItemCustomIntentExtra> customItemIntentExtras;

    private AppItem(String packageName, String activityName, String displayName, Intent intent) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.displayName = displayName;
        this.intent = intent;

        isCustomItem = false;
        customItemIdentifier = null;
        customItemDisplayName = null;
        customItemActivityName = null;
        customItemIntentExtras = null;
    }

    /**
     * @param identifier must be null when creating new custom AppItem, must be set when restoring previously created AppItem.
     * @param targetPackage only used for setting a fallback icon, not used for launching.
     * @param targetActivity must be a full class path (e.g., <code>com.sinu.molla.MainActivity</code>)
     */
    public AppItem(String identifier, String appName, String displayName, String targetPackage, String appActivity, String targetActivity, ArrayList<AppItemCustomIntentExtra> intentExtras) {
        packageName = targetPackage;
        activityName = appActivity;
        this.displayName = appName;

        isCustomItem = true;
        customItemIdentifier = (identifier != null ? identifier : UUID.randomUUID().toString());
        customItemDisplayName = (displayName != null ? displayName : appName);
        customItemActivityName = (targetActivity != null ? targetActivity : appActivity);
        customItemIntentExtras = (intentExtras == null ? new ArrayList<>() : intentExtras);

        intent = new Intent();
        intent.setClassName(packageName, customItemActivityName);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof AppItem)) return false;
        AppItem other = (AppItem)obj;
        if (isCustomItem != other.isCustomItem) return false;
        if (isCustomItem) {
            return customItemIdentifier.equals(other.customItemIdentifier);
        } else {
            return packageName.equals(other.packageName) && activityName.equals(other.activityName);
        }
    }

    public static int compare(AppItem o1, AppItem o2) {
        if (o1.isCustomItem != o2.isCustomItem) return 1;
        if (o1.isCustomItem) {
            return o1.customItemIdentifier.compareTo(o2.customItemIdentifier);
        } else {
            if (o1.packageName.equals(o2.packageName)) return o1.activityName.compareTo(o2.activityName);
            return o1.packageName.compareTo(o2.packageName);
        }
    }

    public static int compareByDisplayName(AppItem o1, AppItem o2) {
        var o1n = o1.customItemDisplayName != null ? o1.customItemDisplayName : o1.displayName;
        var o2n = o2.customItemDisplayName != null ? o2.customItemDisplayName : o2.displayName;
        return o1n.toLowerCase(Locale.ENGLISH).compareTo(o2n.toLowerCase(Locale.ENGLISH));
    }

    public static void launch(Activity activity, AppItem appItem) {
        if (appItem.isCustomItem) {
            Intent i = new Intent();
            i.setClassName(appItem.packageName, appItem.customItemActivityName);
            for (var extra : appItem.customItemIntentExtras) {
                var extraType = extra.getValueType();
                if (extraType == String.class) i.putExtra(extra.getName(), extra.getValueAsString());
                else if (extraType == Integer.class) i.putExtra(extra.getName(), (int)extra.getValueAs(Integer.class));
                else if (extraType == Long.class) i.putExtra(extra.getName(), (long)extra.getValueAs(Long.class));
                else if (extraType == Float.class) i.putExtra(extra.getName(), (float)extra.getValueAs(Float.class));
                else if (extraType == Double.class) i.putExtra(extra.getName(), (double)extra.getValueAs(Double.class));
                else if (extraType == Boolean.class) i.putExtra(extra.getName(), (boolean)extra.getValueAs(Boolean.class));
            }
            try {
                activity.startActivity(i);
            } catch (Exception ignored) {
                //ignored.printStackTrace();
                Toast.makeText(activity, R.string.common_error_app_launch_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (appItem.intent != null) {
                try {
                    activity.startActivity(appItem.intent);
                } catch (Exception ignored) {
                    //ignored.printStackTrace();
                    Toast.makeText(activity, R.string.common_error_app_launch_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static Thread getFetchAppsRunner(Context context, AppItemLoadCompletedCallback callback) {
        return new Thread(() -> {
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

            callback.OnAppItemLoadCompleted(ret);
        });
    }

    private static Thread getFetchTvAppsRunner(Context context, AppItemLoadCompletedCallback callback) {
        return new Thread(() -> {
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

            callback.OnAppItemLoadCompleted(ret);
        });
    }

    public static void fetchAllAppsAsync(Context context, AppItemLoadCompletedCallback callback) {
        Thread thread = new Thread(() -> {
            SharedPreferences pref = context.getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);
            if (pref.getInt("hide_non_tv", 0) == 0) {
                final ArrayList<AppItem> apps = new ArrayList<>();
                final ArrayList<AppItem> tvApps = new ArrayList<>();
                var fetchAppsRunner = getFetchAppsRunner(context, apps::addAll);
                var fetchTvAppsRunner = getFetchTvAppsRunner(context, tvApps::addAll);
                fetchAppsRunner.start();
                fetchTvAppsRunner.start();
                try {
                    fetchAppsRunner.join();
                    fetchTvAppsRunner.join();
                } catch (InterruptedException e) {
                    // let's hope we don't reach here
                    throw new RuntimeException(e);
                }
                ArrayList<AppItem> ret = new ArrayList<>();
                HashSet<String> appsWithTvIcon = new HashSet<>();

                for (AppItem item : tvApps) {
                    appsWithTvIcon.add(item.packageName);
                    ret.add(item);
                }

                for (AppItem item : apps) if (!appsWithTvIcon.contains(item.packageName)) ret.add(item);

                callback.OnAppItemLoadCompleted(ret);
            } else {
                getFetchTvAppsRunner(context, (tvApps) -> {
                    ArrayList<AppItem> ret = new ArrayList<>(tvApps);
                    callback.OnAppItemLoadCompleted(ret);
                });
            }
        });
        thread.start();
    }

    public static void fetchListOfAppsAsync(Context context, List<String> packageNames, AppItemLoadCompletedCallback callback) {
        Thread thread = new Thread(() -> {
            fetchAllAppsAsync(context, (allApps) -> {
                ArrayList<AppItem> ret = new ArrayList<>();
                ArrayList<String> pnames = new ArrayList<>();

                for (int i = 0; i < allApps.size(); i++) {
                    pnames.add(allApps.get(i).packageName);
                }

                for (int i = 0; i < packageNames.size(); i++) {
                    int idx = pnames.indexOf(packageNames.get(i));
                    if (idx != -1) ret.add(allApps.get(idx));
                }

                callback.OnAppItemLoadCompleted(ret);
            });
        });
        thread.start();
    }

    public static JSONObject customItemToJson(AppItem item) throws JSONException {
        if (!item.isCustomItem) return null;
        JSONObject j = new JSONObject();
        j.put("id", item.customItemIdentifier);
        j.put("app_name", item.displayName);
        j.put("display_name", item.customItemDisplayName);
        j.put("package", item.packageName);
        j.put("app_activity", item.activityName);
        j.put("activity", item.customItemActivityName);
        JSONObject extras = new JSONObject();
        for (int i = 0; i < item.customItemIntentExtras.size(); i++) {
            var extra = item.customItemIntentExtras.get(i);
            if (extra.getValueType() == String.class) {
                JSONObject ext = new JSONObject();
                ext.put("type", "string");
                ext.put("value", extra.getValueAsString());
                extras.put(extra.getName(), ext);
            } else if (extra.getValueType() == Integer.class) {
                JSONObject ext = new JSONObject();
                ext.put("type", "int");
                ext.put("value", extra.getValueAsString());
                extras.put(extra.getName(), ext);
            } else if (extra.getValueType() == Long.class) {
                JSONObject ext = new JSONObject();
                ext.put("type", "long");
                ext.put("value", extra.getValueAsString());
                extras.put(extra.getName(), ext);
            } else if (extra.getValueType() == Float.class) {
                JSONObject ext = new JSONObject();
                ext.put("type", "float");
                ext.put("value", extra.getValueAsString());
                extras.put(extra.getName(), ext);
            } else if (extra.getValueType() == Double.class) {
                JSONObject ext = new JSONObject();
                ext.put("type", "double");
                ext.put("value", extra.getValueAsString());
                extras.put(extra.getName(), ext);
            } else if (extra.getValueType() == Boolean.class) {
                JSONObject ext = new JSONObject();
                ext.put("type", "boolean");
                ext.put("value", extra.getValueAsString());
                extras.put(extra.getName(), ext);
            }
        }
        j.put("extras", extras);
        return j;
    }

    public static AppItem jsonToCustomItem(JSONObject j) throws JSONException {
        var id = j.getString("id");
        var appName = j.getString("app_name");
        var displayName = j.getString("display_name");
        var targetPackage = j.getString("package");
        var appActivity = j.getString("app_activity");
        var targetActivity = j.getString("activity");
        var ext = j.getJSONObject("extras");
        ArrayList<AppItemCustomIntentExtra> extras = new ArrayList<>();
        for (Iterator<String> it = ext.keys(); it.hasNext(); ) {
            var extKey = it.next();
            var extCont = ext.getJSONObject(extKey);
            var extType = extCont.getString("type");
            switch (extType) {
                case "string":
                    extras.add(new AppItemCustomIntentExtra(extKey, extCont.getString("value")));
                    break;
                case "int":
                    extras.add(new AppItemCustomIntentExtra(extKey, extCont.getInt("value")));
                    break;
                case "long":
                    extras.add(new AppItemCustomIntentExtra(extKey, extCont.getLong("value")));
                    break;
                case "float":
                    extras.add(new AppItemCustomIntentExtra(extKey, (float)extCont.getDouble("value")));
                    break;
                case "double":
                    extras.add(new AppItemCustomIntentExtra(extKey, extCont.getDouble("value")));
                    break;
                case "boolean":
                    extras.add(new AppItemCustomIntentExtra(extKey, extCont.getBoolean("value")));
                    break;
            }
        }
        return new AppItem(id, appName, displayName, targetPackage, appActivity, targetActivity, extras);
    }
}
