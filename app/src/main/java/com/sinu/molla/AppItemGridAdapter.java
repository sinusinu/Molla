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
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppItemGridAdapter extends RecyclerView.Adapter<AppItemGridAdapter.ViewHolder> {
    private final Context context;
    private final RecyclerView.LayoutManager manager;
    private final ArrayList<AppItem> list;

    private final Animation animScaleUp;
    private final Animation animScaleDown;

    private final Drawable drawableGeneric;

    private OnAppItemFocusChangedListener focusChangedListener;

    public int selectedItem = -1;

    public AppItemGridAdapter(Context context, RecyclerView.LayoutManager manager, ArrayList<AppItem> list) {
        this.list = list;
        this.manager = manager;
        this.context = context;
        animScaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        animScaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down);
        animScaleUp.setFillAfter(true);

        IconCache.kick();
        drawableGeneric = ContextCompat.getDrawable(context, R.drawable.generic);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnHoverListener, View.OnFocusChangeListener {
        public final FrameLayout fvBody;
        public final ImageView ivBanner;
        public final ImageView ivIcon;

        public Intent intent = null;

        public boolean focused = false;

        public ViewHolder(@NonNull View v) {
            super(v);

            fvBody = v.findViewById(R.id.fv_appitem_grid_body);
            ivBanner = v.findViewById(R.id.iv_appitem_grid_banner);
            ivIcon = v.findViewById(R.id.iv_appitem_grid_icon);

            v.setOnFocusChangeListener(this);
            v.setOnHoverListener(this);
            v.setOnClickListener(this);
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                view.setZ(1f);
                if (!focused) view.startAnimation(animScaleUp);
                ((FrameLayout)view).setForeground(ContextCompat.getDrawable(context, R.drawable.outline));
                focused = true;
                selectedItem = manager.getPosition(view);
                if (focusChangedListener != null) {
                    String dispName = selectedItem == list.size() ? context.getString(R.string.main_edit_fav) : list.get(selectedItem).displayName;
                    focusChangedListener.onAppItemFocusChanged(selectedItem, dispName);
                }
            } else {
                view.setZ(0f);
                if (focused) view.startAnimation(animScaleDown);
                ((FrameLayout)view).setForeground(null);
                focused = false;
                if (focusChangedListener != null) focusChangedListener.onAppItemFocusChanged(-1, null);
            }
        }

        @Override
        public boolean onHover(View view, MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onClick(View view) {
            if (intent != null) context.startActivity(intent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appitem_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drawable appBanner = null;
        Drawable appIcon = null;

        holder.fvBody.setContentDescription(list.get(position).displayName);

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

        holder.intent = list.get(position).intent;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnAppItemFocusChangedListener {
        void onAppItemFocusChanged(int index, String displayName);
    }

    public void setOnAppItemFocusChangedListener(OnAppItemFocusChangedListener listener) {
        this.focusChangedListener = listener;
    }
}
