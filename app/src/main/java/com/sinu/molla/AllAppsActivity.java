// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.sinu.molla.databinding.ActivityAllAppsBinding;

import java.util.ArrayList;
import java.util.Collections;

public class AllAppsActivity extends AppCompatActivity {
    ActivityAllAppsBinding binding;

    ArrayList<AppItem> items;
    AppItemGridAdapter adapter;

    int gridRowCount = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllAppsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get screen aspect ratio, use 3-row if aspect ratio is shorter than 16:9
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        float longy = metrics.heightPixels;
        float shorty = metrics.widthPixels;
        if (longy < shorty) { float temp = longy; longy = shorty; shorty = temp; }
        float ratio = longy / shorty;
        if (ratio < 1.7f) gridRowCount = 3;

        new Thread(() -> {
            AppItem.fetchAllAppsAsync(this, (items) -> {
                this.items = items;
                Collections.sort(items, AppItem::compareByDisplayName);

                runOnUiThread(() -> {
                    // TODO: do the thing done with HorizontallyFocusedLinearLayoutManager. for some reason GridLayoutManager won't cooperate.
                    GridLayoutManager manager = new GridLayoutManager(this, gridRowCount);
                    adapter = new AppItemGridAdapter(this, manager, items);

                    adapter.setOnAppItemFocusChangedListener((i, n) -> {
                        binding.tvAllName.setText(n);
                    });

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