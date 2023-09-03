// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sinu.molla.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    SharedPreferences pref;
    boolean isCloseable = false;

    ArrayList<AppItem> items;
    AppItemAdapter adapter;

    private Animation animScaleUp;
    private Animation animScaleDown;

    Handler h;
    Runnable rUpdateStatus;

    Intent batteryStatus;
    boolean batteryExist;
    ConnectivityManager cm;
    WifiManager wm;

    boolean isFavListUpdateReserved = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        items = new ArrayList<>();

        HorizontallyFocusedLinearLayoutManager manager = new HorizontallyFocusedLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false, binding.rvMainFav);
        adapter = new AppItemAdapter(this, manager, items, true);

        adapter.setOnAppItemFocusChangedListener((i, n) -> {
            binding.tvMainFavName.setText(n);
        });

        binding.rvMainFav.setLayoutManager(manager);
        binding.rvMainFav.setAdapter(adapter);

        binding.lvMainAll.setOnClickListener((v) -> {
            startActivity(new Intent(this, AllAppsActivity.class));
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        });
        binding.lvMainSettings.setOnClickListener((v) -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            reserveFavListUpdate();
        });

        animScaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        animScaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        animScaleUp.setFillAfter(true);

        binding.lvMainAll.setOnFocusChangeListener((view, hasFocus) -> {
            binding.lvMainAll.startAnimation(hasFocus ? animScaleUp : animScaleDown);
            binding.lvMainAll.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });
        binding.lvMainSettings.setOnFocusChangeListener((view, hasFocus) -> {
            binding.lvMainSettings.startAnimation(hasFocus ? animScaleUp : animScaleDown);
            binding.lvMainSettings.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int bstatus = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        batteryExist = bstatus != BatteryManager.BATTERY_STATUS_UNKNOWN;
        if (!batteryExist) {
            binding.ivMainBatIcon.setVisibility(View.GONE);
            binding.tvMainBatPercentage.setVisibility(View.GONE);
            binding.spcMainBat.setVisibility(View.GONE);
        } else {
            binding.ivMainBatIcon.setVisibility(View.VISIBLE);
            binding.tvMainBatPercentage.setVisibility(View.VISIBLE);
            binding.spcMainBat.setVisibility(View.VISIBLE);
        }

        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        // must explicitly check this permission, might be missing on devices without wifi
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        } else {
            wm = null;
        }

        rUpdateStatus = () -> {
            batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            if (batteryExist) {
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;

                binding.ivMainBatIcon.setVisibility(View.VISIBLE);
                binding.tvMainBatPercentage.setVisibility(View.VISIBLE);
                binding.spcMainBat.setVisibility(View.VISIBLE);
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_charging));
                } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_full));
                } else {
                    if (batteryPct > 95f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_full));
                    } else if (batteryPct > 80f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_6));
                    } else if (batteryPct > 68f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_5));
                    } else if (batteryPct > 56f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_4));
                    } else if (batteryPct > 44f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_3));
                    } else if (batteryPct > 32f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_2));
                    } else if (batteryPct > 20f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_1));
                    } else if (batteryPct > 10f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_0));
                    } else {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_alert));
                    }
                }

                binding.tvMainBatPercentage.setText(String.format(getString(R.string.main_bat), (int) Math.ceil(batteryPct)));
            }

            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                binding.ivMainConnIcon.setVisibility(View.GONE);
            } else {
                binding.ivMainConnIcon.setVisibility(View.VISIBLE);
                switch (ni.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        // assuming wm is non-null; devices without wifi won't reach here, right?
                        WifiInfo info = wm.getConnectionInfo();
                        int level = WifiManager.calculateSignalLevel(info.getRssi(), 3);
                        switch (level) {
                            case 2:
                                binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_wifi));
                                break;
                            case 1:
                                binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_wifi_low));
                                break;
                            case 0:
                                binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_wifi_verylow));
                                break;
                        }
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_cellular_data));
                        break;
                    default:
                        binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_ethernet));
                        break;
                }

            }

            binding.tvMainTime.setText(DateFormat.getTimeFormat(this).format(new Date()));

            h.postDelayed(rUpdateStatus, 2000);
        };

        h = new Handler(getMainLooper());
    }

    @Override
    public void onBackPressed() {
        if (isCloseable) finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFavListUpdateReserved) {
            binding.pbrMainLoading.setVisibility(View.INVISIBLE);
            binding.rvMainFav.setVisibility(View.INVISIBLE);
            binding.tvMainFavName.setVisibility(View.INVISIBLE);
        }
        h.removeCallbacks(rUpdateStatus);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        String favAppsRaw = pref.getString("fav_apps", "");
        ArrayList<String> favApps = new ArrayList<String>(Arrays.asList(favAppsRaw.split("\\?")));

        if (isFavListUpdateReserved) {
            binding.pbrMainLoading.setVisibility(View.VISIBLE);
            AppItem.fetchListOfAppsAsync(this, favApps, (nitems) -> {
                if (!nitems.equals(items)) {
                    items.clear();
                    items.addAll(nitems);
                }
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    binding.pbrMainLoading.setVisibility(View.GONE);
                    binding.rvMainFav.setVisibility(View.VISIBLE);
                    binding.tvMainFavName.setVisibility(View.VISIBLE);
                    binding.rvMainFav.scrollToPosition(0);
                    binding.rvMainFav.requestFocus();
                });
            });
            isFavListUpdateReserved = false;
        }

        WallpaperHandler.updateWallpaper(this, binding.ivMainWallpaper, false);

        rUpdateStatus.run();

        isCloseable = (pref.getInt("closeable", 0) == 1);
    }

    public void reserveFavListUpdate() {
        isFavListUpdateReserved = true;
    }
}
