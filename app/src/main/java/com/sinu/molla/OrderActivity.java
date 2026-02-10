// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.annotation.SuppressLint;
import android.content.Context;
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
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.sinu.molla.databinding.ActivityOrderBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class OrderActivity extends AppCompatActivity {
    private ActivityOrderBinding binding;

    SharedPreferences pref;

    ArrayList<AppItem> selectedItems;
    AppItemOrderAdapter adapter;

    AppItemOrderAdapter.OnOrderItemClickedListener upClickListener;
    AppItemOrderAdapter.OnOrderItemClickedListener downClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        upClickListener = (view, position) -> {
            if (position == 0) return;
            Collections.swap(selectedItems, position, position - 1);
            adapter.notifyItemRangeChanged(position - 1, 2);
        };

        downClickListener = (view, position) -> {
            if (position == selectedItems.size() - 1) return;
            Collections.swap(selectedItems, position, position + 1);
            adapter.notifyItemRangeChanged(position, 2);
        };

        String favAppsRaw = pref.getString("fav_apps", "");
        ArrayList<String> favApps = new ArrayList<String>(Arrays.asList(favAppsRaw.split("\\?")));

        selectedItems = new ArrayList<>();
        AppItem.fetchAllAppsAsync(this, (r) -> {
            var csm = ((MollaApplication)getApplication()).getCustomShortcutManager();
            selectedItems.clear();
            for (var favApp : favApps) {
                if (favApp.startsWith("custom:")) {
                    var matchingCustomItem = csm.findCustomShortcutById(favApp.substring(7));
                    if (matchingCustomItem != null) selectedItems.add(matchingCustomItem);
                } else {
                    for (var i : r) if (i.packageName.equals(favApp)) selectedItems.add(i);
                }
            }

            runOnUiThread(() -> {
                LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

                adapter = new AppItemOrderAdapter(getApplicationContext(), manager, selectedItems, upClickListener, downClickListener, (pref.getInt("simple_icon_bg", 0) == 1), (pref.getInt("use_focus_outline", 0) == 1));

                binding.rvOrdList.setLayoutManager(manager);
                binding.rvOrdList.setAdapter(adapter);
                if (binding.rvOrdList.getItemAnimator() != null) {
                    ((SimpleItemAnimator)binding.rvOrdList.getItemAnimator()).setSupportsChangeAnimations(false);
                }

                binding.rvOrdList.setVisibility(View.VISIBLE);
                binding.pbrOrdLoading.setVisibility(View.GONE);
            });
        });

        var useFocusOutline = pref.getInt("use_focus_outline", 0) == 1;
        if (useFocusOutline) binding.ivOrdBack.setBackgroundResource(R.drawable.focus_outline);

        binding.ivOrdBack.setOnClickListener((v) -> {
            finish();
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
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
    protected void onPause() {
        super.onPause();

        updatePref();
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
        ((MollaApplication)getApplication()).getWallpaperCache().setWallpaperOnImageView(binding.ivOrdWallpaper, false);
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