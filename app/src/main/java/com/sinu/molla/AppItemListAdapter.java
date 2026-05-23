// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppItemListAdapter extends RecyclerView.Adapter<AppItemListAdapter.ViewHolder> {
    private final Context context;
    private final Activity activity;
    private final ArrayList<AppItem> list;

    private final Animation animScalePressWide;

    private final Drawable drawableGeneric;

    private final boolean useFocusOutline;

    public AppItemListAdapter(Context context, Activity activity, ArrayList<AppItem> list, boolean simple, boolean useFocusOutline) {
        this.list = list;
        this.context = context;
        this.activity = activity;
        this.useFocusOutline = useFocusOutline;
        animScalePressWide = AnimationUtils.loadAnimation(context, R.anim.scale_press_wide);

        drawableGeneric = ContextCompat.getDrawable(context, simple ? R.drawable.generic_simple : R.drawable.generic);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivBanner;
        public final ImageView ivIcon;
        public final TextView tvAppName;

        public ViewHolder(@NonNull View v) {
            super(v);

            ivBanner = v.findViewById(R.id.iv_appitem_list_banner);
            ivIcon = v.findViewById(R.id.iv_appitem_list_icon);
            tvAppName = v.findViewById(R.id.tv_appitem_list_app_name);

            if (useFocusOutline) v.setBackgroundResource(R.drawable.focus_outline);
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
            holder.itemView.startAnimation(animScalePressWide);
            AppItem.launch(activity, list.get(position));
        });
        holder.itemView.setOnLongClickListener(view -> {
            var appItem = list.get(position);
            var viewDetailsDialog = activity.getLayoutInflater().inflate(R.layout.dialog_app_details, null);

            AlertDialog ad = new AlertDialog.Builder(activity)
                    .setView(viewDetailsDialog)
                    .create();

            boolean isNotUninstallable = UninstallabilityChecker.checkUninstallability(context, appItem.packageName) == UninstallabilityChecker.UNINSTALLABILITY_NOT_UNINSTALLABLE;

            ((TextView)(viewDetailsDialog.findViewById(R.id.tv_dialog_app_details_name))).setText(appItem.displayName);
            ((ImageView)(viewDetailsDialog.findViewById(R.id.iv_dialog_app_details_icon))).setImageDrawable(ci.drawable);
            viewDetailsDialog.findViewById(R.id.iv_dialog_app_details_close).setOnClickListener((v) -> {
                ad.dismiss();
            });
            viewDetailsDialog.findViewById(R.id.ll_dialog_app_details_open).setOnClickListener((v) -> {
                AppItem.launch(activity, appItem);
                ad.dismiss();
            });
            viewDetailsDialog.findViewById(R.id.ll_dialog_app_details_app_info).setOnClickListener((v) -> {
                var intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", appItem.packageName, null));
                activity.startActivity(intent);
                ad.dismiss();
            });
            if (isNotUninstallable) {
                viewDetailsDialog.findViewById(R.id.ll_dialog_app_details_uninstall).setVisibility(View.GONE);
            } else {
                viewDetailsDialog.findViewById(R.id.ll_dialog_app_details_uninstall).setVisibility(View.VISIBLE);
                viewDetailsDialog.findViewById(R.id.ll_dialog_app_details_uninstall).setOnClickListener((v) -> {
                    var intent = new Intent(Intent.ACTION_DELETE);
                    intent.setData(Uri.fromParts("package", appItem.packageName, null));
                    activity.startActivity(intent);
                    ad.dismiss();
                });
            }

            ad.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
