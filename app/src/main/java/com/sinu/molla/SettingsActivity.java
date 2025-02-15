// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sinu.molla.databinding.ActivitySettingsBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        SettingsAdapter adapter = new SettingsAdapter(this, pref, manager);
        binding.rvSettingsAllapps.setLayoutManager(manager);
        binding.rvSettingsAllapps.setAdapter(adapter);

        adapter.setOnSettingsClickedListener((idx, key) -> {
            switch (key) {
                case "wallpaper":
                    try {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, 1);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, getString(R.string.settings_error_wallpaper_no_picker), Toast.LENGTH_LONG).show();
                    }
                    break;
                case "hide_non_tv":
                case "closeable":
                    adapter.settings[idx].fetch(pref);
                    if (adapter.settings[idx].value == 0) adapter.settings[idx].set(pref, 1);
                    else adapter.settings[idx].set(pref, 0);
                    adapter.notifyItemChanged(idx);
                    break;
                case "about":
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
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

    @Override
    protected void onResume() {
        super.onResume();

        WallpaperHandler.updateWallpaper(this, binding.ivSettingsWallpaper, false);
    }
}