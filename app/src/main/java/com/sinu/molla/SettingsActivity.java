// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.sinu.molla.databinding.ActivitySettingsBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SettingsActivity extends AppCompatActivity {
    private final int PICK_WALLPAPER = 1;

    ActivitySettingsBinding binding;

    SharedPreferences pref;

    int aboutPressCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        SettingsAdapter adapter = new SettingsAdapter(this, pref, manager);
        binding.rvSettingsAllapps.setLayoutManager(manager);
        binding.rvSettingsAllapps.setAdapter(adapter);
        if (binding.rvSettingsAllapps.getItemAnimator() != null) {
            ((SimpleItemAnimator)binding.rvSettingsAllapps.getItemAnimator()).setSupportsChangeAnimations(false);
        }

        adapter.setOnSettingsClickedListener((idx, key) -> {
            switch (key) {
                case "wallpaper":
                    try {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, PICK_WALLPAPER);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, getString(R.string.settings_error_wallpaper_no_picker), Toast.LENGTH_LONG).show();
                    }
                    break;
                case "hide_non_tv":
                case "closeable":
                case "simple_icon_bg":
                    adapter.settings[idx].fetch(pref);
                    if (adapter.settings[idx].value == 0) {
                        adapter.settings[idx].set(pref, 1);
                    } else {
                        adapter.settings[idx].set(pref, 0);
                    }
                    adapter.notifyItemChanged(idx);
                    break;
                case "autostart":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(this)) {
                        var adAutoStartPermNotify = new AlertDialog.Builder(this)
                                .setTitle(R.string.settings_autostart_at_boot_perm_title)
                                .setMessage(R.string.settings_autostart_at_boot_perm_content)
                                .setPositiveButton(R.string.common_ok, (d, i) -> {
                                    Intent overlayPermScreen = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                    startActivity(overlayPermScreen);
                                })
                                .setNegativeButton(R.string.common_cancel, (d, i) -> {})
                                .create();
                        adAutoStartPermNotify.show();
                    } else {
                        adapter.settings[idx].fetch(pref);
                        if (adapter.settings[idx].value == 0) {
                            adapter.settings[idx].set(pref, 1);
                        } else {
                            adapter.settings[idx].set(pref, 0);
                        }
                        adapter.notifyItemChanged(idx);
                    }
                    break;
                case "indicator":
                    final var dialogCustomizeIndicators = getLayoutInflater().inflate(R.layout.dialog_customize_indicators, null);
                    var adCustomizeIndicator = new AlertDialog.Builder(this)
                            .setView(dialogCustomizeIndicators)
                            .setPositiveButton(R.string.common_ok, (d, i) -> {
                                boolean showBt = ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_bt)).isChecked();
                                boolean showNet = ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_net)).isChecked();
                                boolean showBat = ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_bat)).isChecked();
                                boolean showTime = ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_time)).isChecked();
                                pref.edit()
                                        .putBoolean("indicator_show_bt", showBt)
                                        .putBoolean("indicator_show_net", showNet)
                                        .putBoolean("indicator_show_bat", showBat)
                                        .putBoolean("indicator_show_time", showTime)
                                        .apply();
                            })
                            .setNegativeButton(R.string.common_cancel, (d, i) -> {})
                            .create();
                    adCustomizeIndicator.show();
                    boolean showBt = pref.getBoolean("indicator_show_bt", true);
                    boolean showNet = pref.getBoolean("indicator_show_net", true);
                    boolean showBat = pref.getBoolean("indicator_show_bat", true);
                    boolean showTime = pref.getBoolean("indicator_show_time", true);
                    ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_bt)).setOnCheckedChangeListener((b, v) -> {
                        dialogCustomizeIndicators.findViewById(R.id.iv_dialog_ci_preview_bt).setVisibility(v ? View.VISIBLE : View.GONE);
                    });
                    ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_bt)).setChecked(showBt);
                    dialogCustomizeIndicators.findViewById(R.id.iv_dialog_ci_preview_bt).setVisibility(showBt ? View.VISIBLE : View.GONE);
                    ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_net)).setOnCheckedChangeListener((b, v) -> {
                        dialogCustomizeIndicators.findViewById(R.id.iv_dialog_ci_preview_net).setVisibility(v ? View.VISIBLE : View.GONE);
                    });
                    ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_net)).setChecked(showNet);
                    dialogCustomizeIndicators.findViewById(R.id.iv_dialog_ci_preview_net).setVisibility(showNet ? View.VISIBLE : View.GONE);
                    ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_bat)).setOnCheckedChangeListener((b, v) -> {
                        dialogCustomizeIndicators.findViewById(R.id.iv_dialog_ci_preview_bat).setVisibility(v ? View.VISIBLE : View.GONE);
                        dialogCustomizeIndicators.findViewById(R.id.tv_dialog_ci_preview_bat_perc).setVisibility(v ? View.VISIBLE : View.GONE);
                    });
                    ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_bat)).setChecked(showBat);
                    dialogCustomizeIndicators.findViewById(R.id.iv_dialog_ci_preview_bat).setVisibility(showBat ? View.VISIBLE : View.GONE);
                    dialogCustomizeIndicators.findViewById(R.id.tv_dialog_ci_preview_bat_perc).setVisibility(showBat ? View.VISIBLE : View.GONE);
                    ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_time)).setOnCheckedChangeListener((b, v) -> {
                        dialogCustomizeIndicators.findViewById(R.id.tv_dialog_ci_preview_time).setVisibility(v ? View.VISIBLE : View.GONE);
                    });
                    ((CheckBox)dialogCustomizeIndicators.findViewById(R.id.cb_dialog_time)).setChecked(showTime);
                    dialogCustomizeIndicators.findViewById(R.id.tv_dialog_ci_preview_time).setVisibility(showTime ? View.VISIBLE : View.GONE);
                    break;
                case "orientation":
                    final var dialogOrientation = getLayoutInflater().inflate(R.layout.dialog_orientation, null);
                    @SuppressLint("SourceLockedOrientationActivity")
                    var adOrientation = new AlertDialog.Builder(this)
                            .setView(dialogOrientation)
                            .setPositiveButton(R.string.common_ok, (d, i) -> {
                                boolean orientDisable = ((RadioButton)dialogOrientation.findViewById(R.id.rb_dialog_orient_disable)).isChecked();
                                boolean orientLandscape = ((RadioButton)dialogOrientation.findViewById(R.id.rb_dialog_orient_landscape)).isChecked();
                                boolean orientPortrait = ((RadioButton)dialogOrientation.findViewById(R.id.rb_dialog_orient_portrait)).isChecked();
                                String newOrient = orientLandscape ? "landscape" : (orientPortrait ? "portrait" : "disable");
                                pref.edit()
                                        .putString("forced_orientation", newOrient)
                                        .apply();
                                if (orientLandscape) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                                else if (orientPortrait) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                                else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            })
                            .setNegativeButton(R.string.common_cancel, (d, i) -> {})
                            .create();
                    adOrientation.show();
                    String currentOrient = pref.getString("forced_orientation", "disable");
                    ((RadioButton)dialogOrientation.findViewById(R.id.rb_dialog_orient_disable)).setChecked("disable".equals(currentOrient));
                    ((RadioButton)dialogOrientation.findViewById(R.id.rb_dialog_orient_landscape)).setChecked("landscape".equals(currentOrient));
                    ((RadioButton)dialogOrientation.findViewById(R.id.rb_dialog_orient_portrait)).setChecked("portrait".equals(currentOrient));
                    break;
                case "about":
                    aboutPressCount++;
                    if (aboutPressCount == 7) {
                        aboutPressCount = 0;
                        var currentShowGoofy = pref.getInt("show_goofy", 0);
                        if (currentShowGoofy == 0) {
                            pref.edit().putInt("show_goofy", 1).apply();
                            Toast.makeText(this, R.string.settings_about_secret_enabled, Toast.LENGTH_LONG).show();
                        } else {
                            pref.edit().putInt("show_goofy", 0).apply();
                            Toast.makeText(this, R.string.settings_about_secret_disabled, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case "open_set":
                    try {
                        Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(settingsIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, getString(R.string.settings_error_settings_no_activity), Toast.LENGTH_LONG).show();
                    }
                    break;
                case "open_set_disp":
                    try {
                        Intent settingsIntent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                        startActivity(settingsIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, getString(R.string.settings_error_settings_no_activity), Toast.LENGTH_LONG).show();
                    }
                    break;
                case "open_set_apps":
                    try {
                        Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                        startActivity(settingsIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, getString(R.string.settings_error_settings_no_activity), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        });

        adapter.setOnSettingsLongClickedListener((idx, key) -> {
            if ("wallpaper".equals(key)) {
                File wpFile = new File(getFilesDir(), "wallpaper.webp");
                if (wpFile.exists()) {
                    // noinspection ResultOfMethodCallIgnored
                    wpFile.delete();
                }

                // also delete jpg file if exists
                File wpFileJ = new File(getFilesDir(), "wallpaper.jpg");
                if (wpFileJ.exists()) {
                    // noinspection ResultOfMethodCallIgnored
                    wpFileJ.delete();
                }

                WallpaperHandler.updateWallpaper(this, binding.ivSettingsWallpaper, true);
            }
        });

        binding.ivSettingsBack.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivSettingsBack.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivSettingsBack.setOnClickListener((v) -> {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_WALLPAPER && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                // downscale the bitmap if it's larger than screen size
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;
                if (selectedImage.getWidth() > selectedImage.getHeight()) {
                    if (selectedImage.getWidth() > screenWidth) {
                        Bitmap o = selectedImage;
                        selectedImage = Bitmap.createScaledBitmap(o, screenWidth, screenWidth * o.getHeight() / o.getWidth(), true);
                        o.recycle();
                    }
                } else {
                    if (selectedImage.getHeight() > screenHeight) {
                        Bitmap o = selectedImage;
                        selectedImage = Bitmap.createScaledBitmap(o, screenHeight * o.getWidth() / o.getHeight(), screenHeight, true);
                        o.recycle();
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(new File(getFilesDir(), "wallpaper.webp"))) {
                    selectedImage.compress(Bitmap.CompressFormat.WEBP, 90, fos);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_LONG).show();
                    // Delete the file in case of it being corrupted, deletion result isn't important
                    // noinspection ResultOfMethodCallIgnored
                    new File(getFilesDir(), "wallpaper.webp").delete();
                }

                WallpaperHandler.updateWallpaper(this, binding.ivSettingsWallpaper, true);

                selectedImage.recycle();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onResume() {
        super.onResume();

        String orient = pref.getString("forced_orientation", "disable");
        if ("landscape".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        else if ("portrait".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        WallpaperHandler.updateWallpaper(this, binding.ivSettingsWallpaper, false);
    }
}