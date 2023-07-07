// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppItemListAdapter extends RecyclerView.Adapter<AppItemListAdapter.ViewHolder> {
    private final Context context;
    private final RecyclerView.LayoutManager manager;
    private final ArrayList<AppItem> list;
    private final ArrayList<AppItem> selectedList;

    private final Drawable drawableGeneric;

    private final View.OnClickListener itemClickListener;

    public AppItemListAdapter(Context context, RecyclerView.LayoutManager manager, ArrayList<AppItem> list, ArrayList<AppItem> selectedList, View.OnClickListener itemClickListener) {
        this.list = list;
        this.selectedList = selectedList;
        this.manager = manager;
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

            ivBanner = v.findViewById(R.id.iv_appitem_list_banner);
            ivIcon = v.findViewById(R.id.iv_appitem_list_icon);
            tvAppName = v.findViewById(R.id.tv_appitem_list_app_name);
            cbCheck = v.findViewById(R.id.cb_appitem_list_check);

            v.setOnClickListener(itemClickListener);
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
            if (ci.type == 0) {
                appBanner = ci.drawable;
            } else if (ci.type == 1) {
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
                        IconCache.put(list.get(position).packageName, new AppItemCache(1, appIcon));
                    } else {
                        IconCache.put(list.get(position).packageName, new AppItemCache(0, appBanner));
                    }
                } else {
                    IconCache.put(list.get(position).packageName, new AppItemCache(0, appBanner));
                }
            } catch (PackageManager.NameNotFoundException e) {
                appBanner = null;
                appIcon = null;
                IconCache.put(list.get(position).packageName, new AppItemCache(0, null));
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
