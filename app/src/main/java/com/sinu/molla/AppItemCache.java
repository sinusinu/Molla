// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.graphics.drawable.Drawable;

public class AppItemCache {
    public static final int TYPE_LEANBACK = 0;
    public static final int TYPE_NORMAL = 1;

    public int type;
    public Drawable drawable;

    public AppItemCache(int type, Drawable drawable) {
        this.type = type;
        this.drawable = drawable;
    }
}
