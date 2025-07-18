// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAllAppsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Thread(() -> {
            AppItem.fetchAllAppsAsync(this, (items) -> {
                this.items = items;
                Collections.sort(items, AppItem::compareByDisplayName);

                runOnUiThread(() -> {
                    LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                    adapter = new AppItemListAdapter(this, items);

                    binding.rvAllAllapps.setLayoutManager(manager);
                    binding.rvAllAllapps.setAdapter(adapter);

                    binding.rvAllAllapps.setVisibility(View.VISIBLE);
                    binding.pbrAllLoading.setVisibility(View.GONE);
                });
            });
        }).start();

        binding.ivAllBack.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivAllBack.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivAllBack.setOnClickListener((v) -> {
            finish();
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        });

        // hide system bars
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
    }

    @Override
    protected void onResume() {
        super.onResume();

        WallpaperHandler.updateWallpaper(this, binding.ivAllWallpaper, false);
    }
}