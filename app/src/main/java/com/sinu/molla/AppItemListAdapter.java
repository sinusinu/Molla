// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppItemListAdapter extends RecyclerView.Adapter<AppItemListAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<AppItem> list;

    private Drawable drawableGeneric;

    public AppItemListAdapter(Context context, ArrayList<AppItem> list, boolean simple) {
        this.list = list;
        this.context = context;

        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);
    }

    public void SetSimpleBackground(boolean simple) {
        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivBanner;
        public final ImageView ivIcon;
        public final TextView tvAppName;

        public ViewHolder(@NonNull View v) {
            super(v);

            ivBanner = v.findViewById(R.id.iv_appitem_list_banner);
            ivIcon = v.findViewById(R.id.iv_appitem_list_icon);
            tvAppName = v.findViewById(R.id.tv_appitem_list_app_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appitem_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drawable appBanner = null;
        Drawable appIcon = null;
        if (IconCache.containsKey(list.get(position).packageName)) {
            AppItemCache ci = IconCache.get(list.get(position).packageName);
            if (ci.type == AppItemCache.TYPE_LEANBACK) {
                appBanner = ci.drawable;
            } else if (ci.type == AppItemCache.TYPE_NORMAL) {
                appBanner = drawableGeneric;
                appIcon = ci.drawable;
            }
        } else {
            try {
                appBanner = context.getPackageManager().getApplicationBanner(list.get(position).packageName);
                if (appBanner == null) {
                    appBanner = context.getPackageManager().getActivityBanner(list.get(position).intent);
                    if (appBanner == null) {
                        appBanner = drawableGeneric;
                        appIcon = context.getPackageManager().getApplicationIcon(list.get(position).packageName);
                        IconCache.put(list.get(position).packageName, new AppItemCache(AppItemCache.TYPE_NORMAL, appIcon));
                    } else {
                        IconCache.put(list.get(position).packageName, new AppItemCache(AppItemCache.TYPE_LEANBACK, appBanner));
                    }
                } else {
                    IconCache.put(list.get(position).packageName, new AppItemCache(AppItemCache.TYPE_LEANBACK, appBanner));
                }
            } catch (PackageManager.NameNotFoundException e) {
                appBanner = null;
                appIcon = null;
                IconCache.put(list.get(position).packageName, new AppItemCache(AppItemCache.TYPE_LEANBACK, null));
            }
        }
        holder.ivBanner.setImageDrawable(appBanner);
        holder.ivIcon.setImageDrawable(appIcon);
        holder.tvAppName.setText(list.get(position).displayName);
        holder.itemView.setOnClickListener(view -> {
            if (list.get(position).intent != null) context.startActivity(list.get(position).intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
