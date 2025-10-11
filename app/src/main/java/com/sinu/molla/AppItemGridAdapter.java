// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.app.Activity;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppItemGridAdapter extends RecyclerView.Adapter<AppItemGridAdapter.ViewHolder> {
    private final Context context;
    private final Activity activity;
    private final RecyclerView.LayoutManager manager;
    private final ArrayList<AppItem> list;

    private final Animation animScaleUp;
    private final Animation animScaleDown;

    private Drawable drawableGeneric;

    private OnAppItemFocusChangedListener focusChangedListener;

    public int selectedItem = -1;

    public AppItemGridAdapter(Context context, Activity activity, RecyclerView.LayoutManager manager, ArrayList<AppItem> list, boolean simple) {
        this.list = list;
        this.manager = manager;
        this.context = context;
        this.activity = activity;
        animScaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        animScaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down);
        animScaleUp.setFillAfter(true);

        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);
    }

    public void SetSimpleBackground(boolean simple) {
        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnHoverListener, View.OnFocusChangeListener {
        public final FrameLayout fvBody;
        public final CardView cvCard;
        public final ImageView ivBanner;
        public final ImageView ivIcon;

        public Intent intent = null;

        public boolean focused = false;

        public ViewHolder(@NonNull View v) {
            super(v);

            fvBody = v.findViewById(R.id.fv_appitem_grid_body);
            cvCard = v.findViewById(R.id.cv_appitem_grid_card);
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
                if (!focused) cvCard.startAnimation(animScaleUp);
                cvCard.setForeground(ContextCompat.getDrawable(context, R.drawable.outline));
                focused = true;
                selectedItem = manager.getPosition(view);
                if (focusChangedListener != null) {
                    String dispName = selectedItem == list.size() ? context.getString(R.string.main_edit_fav) : list.get(selectedItem).displayName;
                    focusChangedListener.onAppItemFocusChanged(selectedItem, dispName);
                }
            } else {
                view.setZ(0f);
                if (focused) cvCard.startAnimation(animScaleDown);
                cvCard.setForeground(null);
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
            if (intent != null) activity.startActivity(intent);
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

        var ci = AppItemIcon.getAppItemIcon((MollaApplication)context, list.get(position));
        if (ci.type == AppItemIcon.IconType.LEANBACK) {
            appBanner = ci.drawable;
        } else {
            appBanner = drawableGeneric;
            appIcon = ci.drawable;
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
