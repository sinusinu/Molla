// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.SharedPreferences;

public class MollaSetting {
    public static final int TYPE_BUTTON = 0;
    public static final int TYPE_CHECKBOX = 1;
    public static final int TYPE_CATEGORY = 2;

    public String title;
    public String desc;
    public int type;
    public String key;
    public int value;
    public boolean goofy;

    public MollaSetting(String title, String desc, int type, String key, boolean goofy) {
        this.title = title;
        this.desc = desc;
        this.type = type;
        this.key = key;
        this.goofy = goofy;
    }

    public void fetch(SharedPreferences pref) {
        if (this.type == TYPE_CHECKBOX) this.value = pref.getInt(key, 0);
    }

    public void set(SharedPreferences pref, int value) {
        if (this.type == TYPE_CHECKBOX) {
            this.value = value;
            pref.edit().putInt(key, value).apply();
        }
    }
}
