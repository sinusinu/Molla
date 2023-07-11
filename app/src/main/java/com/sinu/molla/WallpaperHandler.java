// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import java.io.File;

public class WallpaperHandler {
    public static BitmapDrawable wallpaper = null;
    public static boolean isWallpaperCached = false; // this will reset to false if static variables go boom...right?

    public static void updateWallpaper(Context context, ImageView wallpaperTarget, boolean shouldInvalidateCache) {
        if (shouldInvalidateCache || !isWallpaperCached) {
            if (wallpaper != null) {
                wallpaperTarget.setImageDrawable(null);
                wallpaper.getBitmap().recycle();
                wallpaper = null;
            }

            if (new File(context.getFilesDir(), "wallpaper.jpg").exists()) {
                Bitmap b = BitmapFactory.decodeFile(new File(context.getFilesDir(), "wallpaper.jpg").getAbsolutePath());
                wallpaper = new BitmapDrawable(context.getResources(), b);
            } else {
                wallpaper = null;
            }

            isWallpaperCached = true;
        }

        wallpaperTarget.setImageDrawable(wallpaper);
    }
}
