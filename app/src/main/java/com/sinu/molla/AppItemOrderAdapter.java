// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
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

    private Drawable drawableGeneric;

    private final OnOrderItemClickedListener upClickListener;
    private final OnOrderItemClickedListener downClickListener;

    public final boolean useFocusOutline;

    public interface OnOrderItemClickedListener {
        void onOrderItemClicked(View v, int position);
    }

    public AppItemOrderAdapter(Context context, RecyclerView.LayoutManager manager, ArrayList<AppItem> list, OnOrderItemClickedListener upClickListener, OnOrderItemClickedListener downClickListener, boolean simple, boolean useFocusOutline) {
        this.list = list;
        this.manager = manager;
        this.context = context;
        this.useFocusOutline = useFocusOutline;

        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);

        this.upClickListener = upClickListener;
        this.downClickListener = downClickListener;
    }

    public void SetSimpleBackground(boolean simple) {
        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);
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

            ivUp.setOnClickListener((vw) -> {
                upClickListener.onOrderItemClicked(vw, manager.getPosition(v));
            });
            ivDown.setOnClickListener((vw) -> {
                downClickListener.onOrderItemClicked(vw, manager.getPosition(v));
            });

            if (useFocusOutline) {
                ivUp.setBackgroundResource(R.drawable.focus_outline);
                ivDown.setBackgroundResource(R.drawable.focus_outline);
            }
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

        var ci = AppItemIcon.getAppItemIcon((MollaApplication)context, list.get(position));
        if (ci.type == AppItemIcon.IconType.LEANBACK) {
            appBanner = ci.drawable;
        } else {
            appBanner = drawableGeneric;
            appIcon = ci.drawable;
        }
        holder.ivBanner.setImageDrawable(appBanner);
        holder.ivIcon.setImageDrawable(appIcon);
        holder.tvAppName.setText(list.get(position).customItemDisplayName == null ? list.get(position).displayName : list.get(position).customItemDisplayName);

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
