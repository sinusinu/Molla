// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.graphics.drawable.Drawable;

public class AppItemIcon {
    public enum IconType { LEANBACK, NORMAL }

    public IconType type;
    public Drawable drawable;

    public AppItemIcon(IconType type, Drawable drawable) {
        this.type = type;
        this.drawable = drawable;
    }
}
