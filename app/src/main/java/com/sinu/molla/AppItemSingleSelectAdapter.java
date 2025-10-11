// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppItemSingleSelectAdapter extends RecyclerView.Adapter<AppItemSingleSelectAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<AppItem> list;
    private AppItem selectedItem = null;

    private Drawable drawableGeneric;

    private final View.OnClickListener itemClickListener;

    public AppItemSingleSelectAdapter(Context context, ArrayList<AppItem> list, View.OnClickListener itemClickListener, boolean simple) {
        this.list = list;
        this.context = context;

        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);

        this.itemClickListener = itemClickListener;
    }

    public void setSelectedItem(AppItem item) {
        selectedItem = item;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivIcon;
        public final TextView tvAppName;
        public final RadioButton rbCheck;

        public boolean focused = false;

        public ViewHolder(@NonNull View v) {
            super(v);

            ivIcon = v.findViewById(R.id.iv_appitem_single_select_icon);
            tvAppName = v.findViewById(R.id.tv_appitem_single_select_app_name);
            rbCheck = v.findViewById(R.id.rb_appitem_single_select_check);

            v.setOnClickListener(itemClickListener);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appitem_single_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (list.get(position) == null) {
            holder.ivIcon.setImageResource(R.drawable.ic_disable);
            holder.tvAppName.setText(context.getString(R.string.dialog_autolaunch_select_disable));
            holder.rbCheck.setChecked(selectedItem == null);
        } else {
            Drawable appIcon = null;
            var ci = ((MollaApplication)context).getCachedAppIcon(list.get(position).packageName);
            if (ci != null && ci.type == AppItemIcon.IconType.NORMAL) {
                appIcon = ci.drawable;
            } else {
                try {
                    appIcon = context.getPackageManager().getApplicationIcon(list.get(position).packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    // appIcon = null;
                }
            }
            holder.ivIcon.setImageDrawable(appIcon);
            holder.tvAppName.setText(list.get(position).displayName);
            holder.rbCheck.setChecked(list.get(position).equals(selectedItem));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
