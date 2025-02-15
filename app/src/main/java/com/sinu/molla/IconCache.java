// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import java.util.HashMap;

public class IconCache {
    private static HashMap<String, AppItemCache> iconCache;

    public static AppItemCache get(String key) {
        if (iconCache == null) iconCache = new HashMap<>();
        return iconCache.get(key);
    }

    public static boolean containsKey(String key) {
        if (iconCache == null) iconCache = new HashMap<>();
        return iconCache.containsKey(key);
    }

    public static void put(String key, AppItemCache value) {
        if (iconCache == null) iconCache = new HashMap<>();
        iconCache.put(key, value);
    }
}
