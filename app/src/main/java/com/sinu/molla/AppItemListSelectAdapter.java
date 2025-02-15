// Copyright 2022-2023 Woohyun Shin (sinusinu)
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

public class AppItemListSelectAdapter extends RecyclerView.Adapter<AppItemListSelectAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<AppItem> list;
    private final ArrayList<AppItem> selectedList;

    private final Drawable drawableGeneric;

    private final View.OnClickListener itemClickListener;

    public AppItemListSelectAdapter(Context context, ArrayList<AppItem> list, ArrayList<AppItem> selectedList, View.OnClickListener itemClickListener) {
        this.list = list;
        this.selectedList = selectedList;
        this.context = context;

        IconCache.kick();
        drawableGeneric = ContextCompat.getDrawable(context, R.drawable.generic);

        this.itemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivBanner;
        public final ImageView ivIcon;
        public final TextView tvAppName;
        public final CheckBox cbCheck;

        public boolean focused = false;

        public ViewHolder(@NonNull View v) {
            super(v);

            ivBanner = v.findViewById(R.id.iv_appitem_list_select_banner);
            ivIcon = v.findViewById(R.id.iv_appitem_list_select_icon);
            tvAppName = v.findViewById(R.id.tv_appitem_list_select_app_name);
            cbCheck = v.findViewById(R.id.cb_appitem_list_select_check);

            v.setOnClickListener(itemClickListener);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appitem_list_select, parent, false);
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
        holder.cbCheck.setChecked(selectedList.contains(list.get(position)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
