package com.sinu.molla;

import android.app.Application;

import java.util.HashMap;

public class MollaApplication extends Application {
    private WallpaperCache wallpaperCache;
    private HashMap<String, AppItemIcon> iconCache;
    private CustomItemManager customItemManager;

    @Override
    public void onCreate() {
        super.onCreate();

        wallpaperCache = new WallpaperCache(getApplicationContext());
        iconCache = new HashMap<>();
        customItemManager = new CustomItemManager(this);
    }

    public WallpaperCache getWallpaperCache() {
        return wallpaperCache;
    }

    public AppItemIcon getCachedAppIcon(String key) {
        if (!iconCache.containsKey(key)) return null;
        return iconCache.get(key);
    }

    public void cacheAppIcon(String key, AppItemIcon value) {
        iconCache.put(key, value);
    }

    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        iconCache.clear();
    }
}
