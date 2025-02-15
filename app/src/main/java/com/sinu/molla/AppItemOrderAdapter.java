// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppItemOrderAdapter extends RecyclerView.Adapter<AppItemOrderAdapter.ViewHolder> {
    private final Context context;
    private final RecyclerView.LayoutManager manager;
    private final ArrayList<AppItem> list;

    private final Drawable drawableGeneric;

    private final OnOrderItemClickedListener upClickListener;
    private final OnOrderItemClickedListener downClickListener;

    public interface OnOrderItemClickedListener {
        public void onOrderItemClicked(View v, int position);
    }

    public AppItemOrderAdapter(Context context, RecyclerView.LayoutManager manager, ArrayList<AppItem> list, OnOrderItemClickedListener upClickListener, OnOrderItemClickedListener downClickListener) {
        this.list = list;
        this.manager = manager;
        this.context = context;

        drawableGeneric = ContextCompat.getDrawable(context, R.drawable.generic);

        this.upClickListener = upClickListener;
        this.downClickListener = downClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivBanner;
        public final ImageView ivIcon;
        public final TextView tvAppName;
        public final ImageView ivUp;
        public final ImageView ivDown;

        public ViewHolder(@NonNull View v) {
            super(v);

            ivBanner = v.findViewById(R.id.iv_appitem_order_banner);
            ivIcon = v.findViewById(R.id.iv_appitem_order_icon);
            tvAppName = v.findViewById(R.id.tv_appitem_order_app_name);
            ivUp = v.findViewById(R.id.iv_appitem_order_up);
            ivDown = v.findViewById(R.id.iv_appitem_order_down);

            ivUp.setOnFocusChangeListener((view, hasFocus) -> ivUp.setBackgroundColor(context.getColor(hasFocus ? R.color.transparent_white : R.color.transparent)));
            ivDown.setOnFocusChangeListener((view, hasFocus) -> ivDown.setBackgroundColor(context.getColor(hasFocus ? R.color.transparent_white : R.color.transparent)));

            ivUp.setOnClickListener((vw) -> {
                upClickListener.onOrderItemClicked(vw, manager.getPosition(v));
            });
            ivDown.setOnClickListener((vw) -> {
                downClickListener.onOrderItemClicked(vw, manager.getPosition(v));
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appitem_order, parent, false);
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

        if (position == 0) {
            holder.ivUp.setImageAlpha(64);
            holder.ivDown.setImageAlpha(255);
        } else if (position == list.size() - 1) {
            holder.ivUp.setImageAlpha(255);
            holder.ivDown.setImageAlpha(64);
        } else {
            holder.ivUp.setImageAlpha(255);
            holder.ivDown.setImageAlpha(255);
        }
        //holder.cbCheck.setChecked(selectedList.contains(list.get(position)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
