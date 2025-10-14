package com.sinu.molla;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CustomItemManager {
    private Context context;
    private ArrayList<AppItem> customShortcuts;

    public CustomItemManager(Context context) {
        this.context = context;
        customShortcuts = new ArrayList<>();
        loadCustomShortcuts();
    }

    public ArrayList<AppItem> getCustomShortcuts() {
        return customShortcuts;
    }

    public void addCustomShortcut(AppItem item) {
        if (!item.isCustomItem) throw new IllegalArgumentException("Not a custom AppItem");
        customShortcuts.add(item);
        saveCustomShortcuts();
    }

    public void replaceCustomShortcutAt(int index, AppItem item) {
        customShortcuts.remove(index);
        customShortcuts.add(index, item);
        saveCustomShortcuts();
    }

    public void removeCustomShortcutAt(int index) {
        customShortcuts.remove(index);
        saveCustomShortcuts();
    }

    public AppItem findCustomShortcutById(String id) {
        for (int i = 0; i < customShortcuts.size(); i++) {
            if (customShortcuts.get(i).customItemIdentifier.equals(id)) return customShortcuts.get(i);
        }
        return null;
    }

    public void saveCustomShortcuts() {
        var csa = new JSONArray();
        for (int i = 0; i < customShortcuts.size(); i++) {
            try {
                var cs = AppItem.customItemToJson(customShortcuts.get(i));
                csa.put(cs);
            } catch (JSONException ignored) {}
        }
        var csFile = new File(context.getFilesDir(), "custom_shortcuts.json");
        try (FileOutputStream f = new FileOutputStream(csFile, false)) {
            f.write(csa.toString().getBytes(StandardCharsets.UTF_8));
            f.flush();
        } catch (IOException ignored) {}
    }

    private void loadCustomShortcuts() {
        customShortcuts.clear();
        var csFile = new File(context.getFilesDir(), "custom_shortcuts.json");
        var sbCsJson = new StringBuilder();
        if (csFile.exists()) {
            try (FileInputStream f = new FileInputStream(csFile); InputStreamReader i = new InputStreamReader(f, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(i)) {
                String line;
                while ((line = br.readLine()) != null) {
                    sbCsJson.append(line).append('\n');
                }
            } catch (IOException ignored) {}
        }
        if (sbCsJson.length() == 0) return;
        var csJson = sbCsJson.toString();
        try {
            var csa = new JSONArray(csJson);
            for (int i = 0; i < csa.length(); i++) {
                var csi = csa.getJSONObject(i);
                try {
                    var cs = AppItem.jsonToCustomItem(csi);
                    customShortcuts.add(cs);
                } catch (JSONException ignored) {}
            }
        } catch (JSONException ignored) {}
    }
}
