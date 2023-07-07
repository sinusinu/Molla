// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;

public class Util {
    public static void updateWallpaper(Context context, ImageView wallpaperTarget) {
        if (new File(context.getFilesDir(), "wallpaper.png").exists()) {
            Bitmap b = BitmapFactory.decodeFile(new File(context.getFilesDir(), "wallpaper.png").getAbsolutePath());
            wallpaperTarget.setImageBitmap(b);
        } else {
            wallpaperTarget.setImageDrawable(null);
        }
    }
}
