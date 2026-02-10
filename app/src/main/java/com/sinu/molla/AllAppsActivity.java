// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sinu.molla.databinding.ActivityAllAppsBinding;

import java.util.ArrayList;
import java.util.Collections;

public class AllAppsActivity extends AppCompatActivity {
    ActivityAllAppsBinding binding;

    ArrayList<AppItem> items;
    AppItemListAdapter adapter;

    SharedPreferences pref;

    boolean isListUpdateReserved = true;

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
}