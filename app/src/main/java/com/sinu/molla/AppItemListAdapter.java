// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppItemListAdapter extends RecyclerView.Adapter<AppItemListAdapter.ViewHolder> {
    private final Context context;
    private final Activity activity;
    private final ArrayList<AppItem> list;

    private Drawable drawableGeneric;

    public AppItemListAdapter(Context context, Activity activity, ArrayList<AppItem> list, boolean simple) {
        this.list = list;
        this.context = context;
        this.activity = activity;

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
        holder.itemView.setOnClickListener(view -> {
            if (list.get(position).isCustomItem) {
                Intent i = new Intent();
                i.setClassName(list.get(position).packageName, list.get(position).customItemActivityName);
                for (var extra : list.get(position).customItemIntentExtras) {
                    var extraType = extra.getValueType();
                    if (extraType == String.class) i.putExtra(extra.getName(), extra.getValueAsString());
                    else if (extraType == Integer.class) i.putExtra(extra.getName(), (int)extra.getValueAs(Integer.class));
                    else if (extraType == Long.class) i.putExtra(extra.getName(), (long)extra.getValueAs(Long.class));
                    else if (extraType == Float.class) i.putExtra(extra.getName(), (float)extra.getValueAs(Float.class));
                    else if (extraType == Double.class) i.putExtra(extra.getName(), (double)extra.getValueAs(Double.class));
                    else if (extraType == Boolean.class) i.putExtra(extra.getName(), (boolean)extra.getValueAs(Boolean.class));
                }
                try {
                    activity.startActivity(i);
                } catch (Exception ignored) {
                    Toast.makeText(activity, R.string.common_error_app_launch_failed, Toast.LENGTH_SHORT).show();
                }
            } else if (list.get(position).intent != null) {
                try {
                    activity.startActivity(list.get(position).intent);
                } catch (Exception ignored) {
                    Toast.makeText(activity, R.string.common_error_app_launch_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
