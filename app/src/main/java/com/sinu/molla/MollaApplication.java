package com.sinu.molla;

import android.app.Application;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class MollaApplication extends Application {
    private WallpaperCache wallpaperCache;
    private HashMap<String, AppItemIcon> iconCache;
    private ArrayList<AppItem> customShortcuts;

    @Override
    public void onCreate() {
        super.onCreate();

        wallpaperCache = new WallpaperCache(getApplicationContext());
        iconCache = new HashMap<>();
        customShortcuts = new ArrayList<>();
        loadCustomShortcuts();
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

    public ArrayList<AppItem> getCustomShortcuts() {
        return customShortcuts;
    }

    public void addCustomShortcut(AppItem item) {
        if (!item.isCustomItem) throw new IllegalArgumentException("Not a custom AppItem");
        customShortcuts.add(item);
        saveCustomShortcuts();
    }

    public void removeCustomShortcutAt(int index) {
        customShortcuts.remove(index);
        saveCustomShortcuts();
    }

    public void saveCustomShortcuts() {
        var csa = new JSONArray();
        for (int i = 0; i < customShortcuts.size(); i++) {
            try {
                var cs = AppItem.customItemToJson(customShortcuts.get(i));
                csa.put(cs);
            } catch (JSONException ignored) {Log.e("saveCustomShortcut", "Error in b a!");}
        }
        var csFile = new File(getFilesDir(), "custom_shortcuts.json");
        try (FileOutputStream f = new FileOutputStream(csFile, false)) {
            f.write(csa.toString().getBytes(StandardCharsets.UTF_8));
            f.flush();
        } catch (IOException ignored) {Log.e("saveCustomShortcut", "Error in b b!");}
    }

    private void loadCustomShortcuts() {
        customShortcuts.clear();
        var csFile = new File(getFilesDir(), "custom_shortcuts.json");
        var sbCsJson = new StringBuilder();
        if (csFile.exists()) {
            try (FileInputStream f = new FileInputStream(csFile); InputStreamReader i = new InputStreamReader(f, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(i)) {
                String line;
                while ((line = br.readLine()) != null) {
                    sbCsJson.append(line).append('\n');
                }
            } catch (IOException e) { Log.e("loadCustomShortcut", "Error in b a!"); return; }
        }
        if (sbCsJson.length() == 0) { Log.e("loadCustomShortcut", "No cs found!"); return; }
        var csJson = sbCsJson.toString();
        try {
            var csa = new JSONArray(csJson);
            for (int i = 0; i < csa.length(); i++) {
                var csi = csa.getJSONObject(i);
                try {
                    var cs = AppItem.jsonToCustomItem(csi);
                    customShortcuts.add(cs);
                } catch (JSONException ignored) {Log.e("loadCustomShortcut", "Error in b c, invalid json!");}
            }
        } catch (JSONException e) {
            Log.e("loadCustomShortcut", "Error in b b!");
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        iconCache.clear();
    }
}
