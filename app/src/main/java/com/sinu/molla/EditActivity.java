// Copyright 2022 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sinu.molla.databinding.ActivityEditBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class EditActivity extends AppCompatActivity {
    ActivityEditBinding binding;

    SharedPreferences pref;

    ArrayList<AppItem> items;
    ArrayList<AppItem> selectedItems;
    AppItemListAdapter adapter;

    View.OnClickListener itemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        String favAppsRaw = pref.getString("fav_apps", "");
        ArrayList<String> favApps = new ArrayList<String>(Arrays.asList(favAppsRaw.split("\\?")));

        AppItem.fetchListOfAppsAsync(this, favApps, (r) -> {
            selectedItems = r;

            AppItem.fetchAllAppsAsync(this, (rr) -> {
                items = rr;
                Collections.sort(items, AppItem::compareByDisplayName);

                runOnUiThread(() -> {
                    LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

                    itemClickListener = view -> {
                        int idx = manager.getPosition(view);
                        AppItem si = items.get(idx);
                        if (selectedItems.contains(si)) selectedItems.remove(si);
                        else selectedItems.add(si);
                        updatePref();
                        adapter.notifyItemChanged(idx);
                    };

                    adapter = new AppItemListAdapter(this, manager, items, selectedItems, itemClickListener);

                    binding.rvEditList.setLayoutManager(manager);
                    binding.rvEditList.setAdapter(adapter);

                    binding.rvEditList.setVisibility(View.VISIBLE);
                    binding.pbrEditLoading.setVisibility(View.GONE);
                });
            });
        });

        binding.ivEditBack.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivEditBack.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivEditBack.setOnClickListener((v) -> {
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

        Util.updateWallpaper(this, binding.ivEditWallpaper);
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