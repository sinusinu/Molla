// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
                    adapter.settings[idx].fetch(pref);
                    if (adapter.settings[idx].value == 0) adapter.settings[idx].set(pref, 1);
                    else adapter.settings[idx].set(pref, 0);
                    adapter.notifyItemChanged(idx);
                    break;
                case "about":
                    break;
            }
        });

        binding.ivSettingsBack.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivSettingsBack.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivSettingsBack.setOnClickListener((v) -> {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                try (FileOutputStream fos = new FileOutputStream(new File(getFilesDir(), "wallpaper.png"))) {
                    selectedImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Util.updateWallpaper(this, binding.ivSettingsWallpaper);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Util.updateWallpaper(this, binding.ivSettingsWallpaper);
    }
}