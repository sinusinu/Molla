// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
    AppItemListAdapter adapter;

    View.OnClickListener itemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        items = new ArrayList<>();
        selectedItems = new ArrayList<>();

        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new AppItemListAdapter(this, manager, items, selectedItems, itemClickListener);
        binding.rvEditList.setLayoutManager(manager);
        binding.rvEditList.setAdapter(adapter);
        binding.rvEditList.setItemAnimator(null);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_REORDER) {
            fetchItems();
        }
    }

    private void fetchItems() {
        String favAppsRaw = pref.getString("fav_apps", "");
        Log.d("Molla", "fetchItems fetched fav_apps " + favAppsRaw);
        ArrayList<String> favApps = new ArrayList<String>(Arrays.asList(favAppsRaw.split("\\?")));

        AppItem.fetchListOfAppsAsync(this, favApps, (r) -> {
            selectedItems.clear();
            selectedItems.addAll(r);

            AppItem.fetchAllAppsAsync(this, (rr) -> {
                items.clear();
                items.addAll(rr);
                Collections.sort(items, AppItem::compareByDisplayName);

                runOnUiThread(() -> {
                    adapter = new AppItemListAdapter(this, manager, items, selectedItems, itemClickListener);
                    binding.rvEditList.setAdapter(adapter);

                    binding.rvEditList.setVisibility(View.VISIBLE);
                    binding.pbrEditLoading.setVisibility(View.GONE);
                });
            });
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

        WallpaperHandler.updateWallpaper(this, binding.ivEditWallpaper, false);
    }

    private void updatePref() {
        StringBuilder sb = new StringBuilder();
        for (AppItem ai : selectedItems) {
            Log.d("Molla", "updatePref: appending " + ai.packageName);
            sb.append(ai.packageName);
            sb.append("?");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        pref.edit().putString("fav_apps", sb.toString()).apply();
    }
}