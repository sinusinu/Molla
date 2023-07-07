// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.graphics.drawable.Drawable;

public class AppItemCache {
    public int type;
    public Drawable drawable;

    public AppItemCache(int type, Drawable drawable) {
        this.type = type;
        this.drawable = drawable;
    }
}
