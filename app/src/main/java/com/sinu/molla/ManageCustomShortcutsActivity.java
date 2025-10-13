package com.sinu.molla;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sinu.molla.databinding.ActivityManageCustomShortcutsBinding;

import java.util.ArrayList;

public class ManageCustomShortcutsActivity extends AppCompatActivity {
    ActivityManageCustomShortcutsBinding binding;

    ArrayList<AppItem> items;
    AppItemListManageCustomAdapter adapter;

    SharedPreferences pref;

    Runnable rUpdateCustomShortcuts;

    View.OnClickListener itemEditListener;
    View.OnClickListener itemDeleteListener;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityManageCustomShortcutsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        items = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvManageCustomShortcutsShortcuts.setLayoutManager(manager);

        itemEditListener = (v) -> {
            var i = manager.getPosition(v);
        };
        itemDeleteListener = (v) -> {
            var i = manager.getPosition(v);
            ((MollaApplication)getApplication()).removeCustomShortcutAt(i);
            rUpdateCustomShortcuts.run();
        };

        rUpdateCustomShortcuts = () -> {
            var cs = ((MollaApplication)getApplication()).getCustomShortcuts();
            items.clear();
            items.addAll(cs);

            adapter = new AppItemListManageCustomAdapter(getApplicationContext(), this, items, itemEditListener, itemDeleteListener, (pref.getInt("simple_icon_bg", 0) == 1));
            binding.rvManageCustomShortcutsShortcuts.setAdapter(adapter);

            binding.rvManageCustomShortcutsShortcuts.setVisibility(View.VISIBLE);
            binding.pbrManageCustomShortcutsLoading.setVisibility(View.GONE);

            adapter.notifyDataSetChanged();
        };

        rUpdateCustomShortcuts.run();

        binding.ivManageCustomShortcutsBack.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivManageCustomShortcutsBack.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivManageCustomShortcutsBack.setOnClickListener((v) -> {
            finish();
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        });

        binding.ivManageCustomShortcutsAdd.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivManageCustomShortcutsAdd.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivManageCustomShortcutsAdd.setOnClickListener((v) -> {
            //((MollaApplication)getApplication()).addCustomShortcut(new AppItem("test", "ass", "com.sinu.molla", "com.sinu.molla.MainActivity", null));
            //rUpdateCustomShortcuts.run();
            var dialog = new AlertDialog.Builder(this)
                    .setView(R.layout.dialog_custom_item)
                    .setPositiveButton(R.string.dialog_custom_item_save, (d, i) -> { d.dismiss(); })
                    .setNegativeButton(R.string.common_cancel, (d, i) -> { d.dismiss(); })
                    .create();
            dialog.show();
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

        //if (adapter != null) adapter.SetSimpleBackground(pref.getInt("simple_icon_bg", 0) == 1);
        ((MollaApplication)getApplication()).getWallpaperCache().setWallpaperOnImageView(binding.ivManageCustomShortcutsWallpaper, false);
    }
}