// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.sinu.molla.databinding.ActivityEditBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class EditActivity extends AppCompatActivity {
    private final int REQ_REORDER = 1;

    ActivityEditBinding binding;

    SharedPreferences pref;

    ArrayList<AppItem> items;
    ArrayList<AppItem> selectedItems;
    LinearLayoutManager manager;
    AppItemListSelectAdapter adapter;

    View.OnClickListener itemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        items = new ArrayList<>();
        selectedItems = new ArrayList<>();

        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new AppItemListSelectAdapter(getApplicationContext(), items, selectedItems, itemClickListener, (pref.getInt("simple_icon_bg", 0) == 1));
        binding.rvEditList.setLayoutManager(manager);
        binding.rvEditList.setAdapter(adapter);
        binding.rvEditList.setItemAnimator(null);
        if (binding.rvEditList.getItemAnimator() != null) {
            ((SimpleItemAnimator)binding.rvEditList.getItemAnimator()).setSupportsChangeAnimations(false);
        }

        itemClickListener = view -> {
            int idx = manager.getPosition(view);
            AppItem si = items.get(idx);
            if (selectedItems.contains(si)) selectedItems.remove(si);
            else selectedItems.add(si);
            updatePref();
            adapter.notifyItemChanged(idx);
        };

        fetchItems();

        binding.ivEditBack.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivEditBack.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivEditBack.setOnClickListener((v) -> {
            finish();
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        });

        binding.ivEditReorder.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivEditReorder.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivEditReorder.setOnClickListener((v) -> {
            if (selectedItems.size() < 2) {
                Toast.makeText(this, getString(R.string.edit_reorder_empty), Toast.LENGTH_SHORT).show();
            } else {
                startActivityForResult(new Intent(this, OrderActivity.class), REQ_REORDER);
                overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_REORDER) {
            fetchItems();
        }
    }

    private void fetchItems() {
        var csm = ((MollaApplication)getApplication()).getCustomShortcutManager();
        var customItems = csm.getCustomShortcuts();
        String favAppsRaw = pref.getString("fav_apps", "");
        ArrayList<String> favApps = new ArrayList<>(Arrays.asList(favAppsRaw.split("\\?")));

        AppItem.fetchAllAppsAsync(this, (rr) -> {
            // this code is written in this way to preserve the order
            selectedItems.clear();
            for (var favApp : favApps) {
                if (favApp.startsWith("custom:")) {
                    var matchingCustomItem = csm.findCustomShortcutById(favApp.substring(7));
                    if (matchingCustomItem != null) selectedItems.add(matchingCustomItem);
                } else {
                    for (var i : rr) if (i.packageName.equals(favApp)) selectedItems.add(i);
                }
            }
            items.clear();
            items.addAll(rr);
            items.addAll(customItems);
            Collections.sort(items, AppItem::compareByDisplayName);

            runOnUiThread(() -> {
                adapter = new AppItemListSelectAdapter(getApplicationContext(), items, selectedItems, itemClickListener, (pref.getInt("simple_icon_bg", 0) == 1));
                binding.rvEditList.setAdapter(adapter);

                binding.rvEditList.setVisibility(View.VISIBLE);
                binding.pbrEditLoading.setVisibility(View.GONE);
            });
        });
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onResume() {
        super.onResume();

        String orient = pref.getString("forced_orientation", "disable");
        if ("landscape".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        else if ("portrait".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (adapter != null) adapter.SetSimpleBackground(pref.getInt("simple_icon_bg", 0) == 1);
        ((MollaApplication)getApplication()).getWallpaperCache().setWallpaperOnImageView(binding.ivEditWallpaper, false);
    }

    private void updatePref() {
        StringBuilder sb = new StringBuilder();
        for (AppItem ai : selectedItems) {
            if (ai.isCustomItem) sb.append("custom:").append(ai.customItemIdentifier);
            else sb.append(ai.packageName);
            sb.append("?");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        pref.edit().putString("fav_apps", sb.toString()).apply();
    }
}