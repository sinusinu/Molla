package com.sinu.molla;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

        AppItem.fetchListOfAppsAsync(this, favApps, (r) -> {
            selectedItems = r;

            runOnUiThread(() -> {
                LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

                adapter = new AppItemOrderAdapter(this, manager, selectedItems, upClickListener, downClickListener);

                binding.rvOrdList.setLayoutManager(manager);
                binding.rvOrdList.setAdapter(adapter);
                binding.rvOrdList.setItemAnimator(null);

                binding.rvOrdList.setVisibility(View.VISIBLE);
                binding.pbrOrdLoading.setVisibility(View.GONE);
            });
        });

        binding.ivOrdBack.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivOrdBack.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivOrdBack.setOnClickListener((v) -> {
            // TODO: save order
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
    protected void onPause() {
        super.onPause();

        updatePref();
    }

    @Override
    protected void onResume() {
        super.onResume();

        WallpaperHandler.updateWallpaper(this, binding.ivOrdWallpaper, false);
    }

    private void updatePref() {
        StringBuilder sb = new StringBuilder();
        for (AppItem ai : selectedItems) {
            sb.append(ai.packageName);
            sb.append("?");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        pref.edit().putString("fav_apps", sb.toString()).apply();
    }
}