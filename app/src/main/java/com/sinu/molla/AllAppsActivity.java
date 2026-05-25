// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sinu.molla.databinding.ActivityAllAppsBinding;

import java.util.ArrayList;
import java.util.Collections;

public class AllAppsActivity extends AppCompatActivity {
    private static final int REQ_APP_REMOVAL_CHECK_NEEDED = 1;

    ActivityAllAppsBinding binding;

    ArrayList<AppItem> items;
    AppItemListAdapter adapter;

    SharedPreferences pref;
    Handler h;

    boolean isListUpdateReserved = true;

    int removalCheckTarget = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAllAppsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        var useFocusOutline = pref.getInt("use_focus_outline", 0) == 1;
        if (useFocusOutline) {
            binding.ivAllBack.setBackgroundResource(R.drawable.focus_outline);
            binding.ivAllManageCustom.setBackgroundResource(R.drawable.focus_outline);
        }

        binding.ivAllBack.setOnClickListener((v) -> {
            finish();
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        });

        binding.ivAllManageCustom.setOnClickListener((v) -> {
            startActivity(new Intent(this, ManageCustomShortcutsActivity.class));
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            isListUpdateReserved = true;
        });

        // hide system bars
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        h = new Handler(getMainLooper());

        var backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onResume() {
        super.onResume();

        String orient = pref.getString("forced_orientation", "disable");
        if ("landscape".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        else if ("portrait".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        ((MollaApplication)getApplication()).getWallpaperCache().setWallpaperOnImageView(binding.ivAllWallpaper, false);

        if (isListUpdateReserved) {
            isListUpdateReserved = false;
            binding.rvAllAllapps.setVisibility(View.GONE);
            binding.pbrAllLoading.setVisibility(View.VISIBLE);

            new Thread(() -> {
                AppItem.fetchAllAppsAsync(this, (items) -> {
                    this.items = items;
                    var customItems = ((MollaApplication) getApplication()).getCustomShortcutManager().getCustomShortcuts();
                    this.items.addAll(customItems);
                    Collections.sort(items, AppItem::compareByDisplayName);

                    runOnUiThread(() -> {
                        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                        adapter = new AppItemListAdapter(getApplicationContext(), this, items, (pref.getInt("simple_icon_bg", 0) == 1), (pref.getInt("use_focus_outline", 0) == 1));

                        binding.rvAllAllapps.setLayoutManager(manager);
                        binding.rvAllAllapps.setAdapter(adapter);

                        binding.rvAllAllapps.setVisibility(View.VISIBLE);
                        binding.pbrAllLoading.setVisibility(View.GONE);
                    });
                });
            }).start();
        }
    }

    @SuppressWarnings("deprecation")
    public void showAppInfo(int index) {
        if (index < 0 || index >= items.size()) return;
        removalCheckTarget = index;
        var appItem = items.get(index);
        var intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", appItem.packageName, null));
        startActivityForResult(intent, REQ_APP_REMOVAL_CHECK_NEEDED);
    }

    @SuppressWarnings("deprecation")
    public void askAppUninstall(int index) {
        if (index < 0 || index >= items.size()) return;
        removalCheckTarget = index;
        var appItem = items.get(index);
        var intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.fromParts("package", appItem.packageName, null));
        startActivityForResult(intent, REQ_APP_REMOVAL_CHECK_NEEDED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_APP_REMOVAL_CHECK_NEEDED) {
            h.postDelayed(() -> {
                if (removalCheckTarget < 0 || removalCheckTarget >= items.size()) return;
                checkIfAppAtIndexIsRemoved(removalCheckTarget);
                removalCheckTarget = -1;
            }, 1000);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void checkIfAppAtIndexIsRemoved(int index) {
        var appItem = items.get(index);
        boolean packageRemoved = false;
        try {
            getPackageManager().getPackageInfo(appItem.packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageRemoved = true;
        }
        if (packageRemoved) {
            items.remove(index);
            adapter.notifyDataSetChanged();
        }
    }
}