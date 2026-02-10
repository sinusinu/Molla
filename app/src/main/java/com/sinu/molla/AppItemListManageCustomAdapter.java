// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.app.Activity;
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

public class AppItemListManageCustomAdapter extends RecyclerView.Adapter<AppItemListManageCustomAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<AppItem> list;

    private final Drawable drawableGeneric;

    private final View.OnClickListener itemEditListener;
    private final View.OnClickListener itemDeleteListener;

    private final boolean useFocusOutline;

    public AppItemListManageCustomAdapter(Context context, ArrayList<AppItem> list, View.OnClickListener itemEditListener, View.OnClickListener itemDeleteListener, boolean simple, boolean useFocusOutline) {
        this.list = list;
        this.context = context;
        this.useFocusOutline = useFocusOutline;

        this.itemEditListener = itemEditListener;
        this.itemDeleteListener = itemDeleteListener;

        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivBanner;
        public final ImageView ivIcon;
        public final TextView tvAppName;
        public final ImageView ivEdit;
        public final ImageView ivDelete;

        public ViewHolder(@NonNull View v) {
            super(v);

            ivBanner = v.findViewById(R.id.iv_appitem_list_manage_custom_banner);
            ivIcon = v.findViewById(R.id.iv_appitem_list_manage_custom_icon);
            tvAppName = v.findViewById(R.id.tv_appitem_list_manage_custom_app_name);
            ivEdit = v.findViewById(R.id.iv_appitem_list_manage_custom_edit);
            ivDelete = v.findViewById(R.id.iv_appitem_list_manage_custom_delete);

            ivEdit.setOnClickListener((e) -> itemEditListener.onClick(v));
            ivDelete.setOnClickListener((e) -> itemDeleteListener.onClick(v));

            if (useFocusOutline) {
                ivEdit.setBackgroundResource(R.drawable.focus_outline);
                ivDelete.setBackgroundResource(R.drawable.focus_outline);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appitem_list_manage_custom, parent, false);
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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
