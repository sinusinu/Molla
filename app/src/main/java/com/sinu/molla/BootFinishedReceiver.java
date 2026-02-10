// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.List;

public class BootFinishedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences pref = context.getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);
            if (pref.getInt("autostart", 0) == 1) {
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("BOOT_COMPLETE_RECEIVED", true);
                context.startActivity(i);
            } else if (pref.getInt("autolaunch_alt_detect", 0) == 1 && pref.getString("autolaunch_package", null) != null) {
                if (isAppRunningInForeground(context)) {
                    Intent i = new Intent(context, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("BOOT_COMPLETE_RECEIVED", true);
                    context.startActivity(i);
                } else {
                    pref.edit().putInt("boot_complete_alt_detect_hint", 1).apply();
                }
            }
        }
    }

    private boolean isAppRunningInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
        if (runningApps == null) return false;
        for (ActivityManager.RunningAppProcessInfo runningApp : runningApps) {
            if (runningApp.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && runningApp.processName.equals("com.sinu.molla")) {
                return true;
            }
        }
        return false;
    }
}