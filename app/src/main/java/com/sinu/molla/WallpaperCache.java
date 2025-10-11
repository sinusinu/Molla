// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

public class WallpaperCache {
    private final Context context;
    private BitmapDrawable wallpaper = null;
    private boolean isWallpaperCached = false;

    public WallpaperCache(Context context) {
        this.context = context;
    }

    public void setWallpaperOnImageView(ImageView wallpaperTarget, boolean shouldInvalidateCache) {
        if (shouldInvalidateCache || !isWallpaperCached) {
            if (wallpaper != null) {
                wallpaperTarget.setImageDrawable(null);
                wallpaper.getBitmap().recycle();
                wallpaper = null;
            }

            if (new File(context.getFilesDir(), "wallpaper.webp").exists()) {
                Bitmap b = BitmapFactory.decodeFile(new File(context.getFilesDir(), "wallpaper.webp").getAbsolutePath());
                wallpaper = new BitmapDrawable(context.getResources(), b);
            } else if (new File(context.getFilesDir(), "wallpaper.jpg").exists()) {
                // found jpg wallpaper, convert to webp
                // jpg wallpaper will be used right now, but webp will be used next time updateWallpaper is called
                Bitmap b = BitmapFactory.decodeFile(new File(context.getFilesDir(), "wallpaper.jpg").getAbsolutePath());
                wallpaper = new BitmapDrawable(context.getResources(), b);

                try (FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), "wallpaper.webp"))) {
                    b.compress(Bitmap.CompressFormat.WEBP, 90, fos);
                } catch (Exception e) {
                    // in any case we land here, let's just keep using jpg wallpaper and try again next time
                    // noinspection ResultOfMethodCallIgnored
                    new File(context.getFilesDir(), "wallpaper.webp").delete();
                }
            } else {
                wallpaper = null;
            }

            isWallpaperCached = true;
        }

        wallpaperTarget.setImageDrawable(wallpaper);
    }
}
