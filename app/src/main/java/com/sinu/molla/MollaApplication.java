package com.sinu.molla;

import android.app.Application;

public class MollaApplication extends Application {
    private WallpaperCache wallpaperCache;

    @Override
    public void onCreate() {
        super.onCreate();

        wallpaperCache = new WallpaperCache(getApplicationContext());
    }

    public WallpaperCache getWallpaperCache() {
        return wallpaperCache;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
