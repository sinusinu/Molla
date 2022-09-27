// Copyright 2022 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import java.util.HashMap;

public class IconCache {
    private static HashMap<String, AppItemCache> iconCache;

    public static void kick() {
        if (iconCache == null) iconCache = new HashMap<>();
    }

    public static AppItemCache get(String key) {
        kick();
        return iconCache.get(key);
    }

    public static boolean containsKey(String key) {
        kick();
        return iconCache.containsKey(key);
    }

    public static void put(String key, AppItemCache value) {
        kick();
        iconCache.put(key, value);
    }
}
