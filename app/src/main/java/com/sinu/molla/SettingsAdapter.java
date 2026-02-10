// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
    Context context;
    SharedPreferences pref;
    RecyclerView.LayoutManager manager;

    public MollaSetting[] settings;

    private SettingsClickedListener clickListener;
    private SettingsLongClickedListener longClickListener;

    private boolean showGoofy = false;
    private boolean useFocusOutline = false;

    public SettingsAdapter(Context context, SharedPreferences pref, RecyclerView.LayoutManager manager) {
        this.context = context;
        this.pref = pref;
        this.manager = manager;

        showGoofy = pref.getInt("show_goofy", 0) == 1;
        useFocusOutline = pref.getInt("use_focus_outline", 0) == 1;

        settings = new MollaSetting[] {
                new MollaSetting(
                        context.getString(R.string.settings_category_appearance),
                        null,
                        MollaSetting.TYPE_CATEGORY, null, false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_wallpaper_title),
                        context.getString(R.string.settings_wallpaper_desc),
                        MollaSetting.TYPE_BUTTON, "wallpaper", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_customize_indicator_title),
                        context.getString(R.string.settings_customize_indicator_desc),
                        MollaSetting.TYPE_BUTTON, "indicator", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_simple_background_title),
                        context.getString(R.string.settings_simple_background_desc),
                        MollaSetting.TYPE_CHECKBOX, "simple_icon_bg", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_use_system_bar_title),
                        context.getString(R.string.settings_use_system_bar_desc),
                        MollaSetting.TYPE_CHECKBOX, "use_system_bar", true
                ),
                new MollaSetting(
                        context.getString(R.string.settings_outline_title),
                        context.getString(R.string.settings_outline_desc),
                        MollaSetting.TYPE_CHECKBOX, "use_focus_outline", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_category_behavior),
                        null,
                        MollaSetting.TYPE_CATEGORY, null, false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_hide_non_tv_apps_title),
                        context.getString(R.string.settings_hide_non_tv_apps_desc),
                        MollaSetting.TYPE_CHECKBOX, "hide_non_tv", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_closeable_title),
                        context.getString(R.string.settings_closeable_desc),
                        MollaSetting.TYPE_CHECKBOX, "closeable", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_autostart_at_boot_title),
                        context.getString(R.string.settings_autostart_at_boot_desc),
                        MollaSetting.TYPE_CHECKBOX, "autostart", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_orientation_title),
                        context.getString(R.string.settings_orientation_desc),
                        MollaSetting.TYPE_BUTTON, "orientation", true
                ),
                new MollaSetting(
                        context.getString(R.string.settings_kiosk_mode_title),
                        context.getString(R.string.settings_kiosk_mode_desc),
                        MollaSetting.TYPE_CHECKBOX, "kiosk_mode", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_category_autolaunch),
                        null,
                        MollaSetting.TYPE_CATEGORY, null, false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_autolaunch_app_title),
                        context.getString(R.string.settings_autolaunch_app_desc),
                        MollaSetting.TYPE_BUTTON, "autolaunch_app", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_autolaunch_delay_title),
                        context.getString(R.string.settings_autolaunch_delay_desc),
                        MollaSetting.TYPE_BUTTON, "autolaunch_delay", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_autolaunch_alt_detect_title),
                        context.getString(R.string.settings_autolaunch_alt_detect_desc),
                        MollaSetting.TYPE_CHECKBOX, "autolaunch_alt_detect", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_category_about),
                        null,
                        MollaSetting.TYPE_CATEGORY, null, false
                ),
                new MollaSetting(
                        String.format(context.getString(R.string.settings_about_title), BuildConfig.VERSION_NAME),
                        context.getString(R.string.settings_about_desc),
                        MollaSetting.TYPE_BUTTON, "about", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_category_system_settings),
                        null,
                        MollaSetting.TYPE_CATEGORY, null, false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_open_system_settings),
                        context.getString(R.string.settings_open_system_settings_desc),
                        MollaSetting.TYPE_BUTTON, "open_set", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_open_system_display),
                        context.getString(R.string.settings_open_system_display_desc),
                        MollaSetting.TYPE_BUTTON, "open_set_disp", false
                ),
                new MollaSetting(
                        context.getString(R.string.settings_open_system_apps),
                        context.getString(R.string.settings_open_system_apps_desc),
                        MollaSetting.TYPE_BUTTON, "open_set_apps", false
                ),
        };
        for (MollaSetting s : settings) s.fetch(pref);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView tvPropertyTitle;
        public TextView tvPropertyDesc;
        public CheckBox cbPropertyCheck;
        public TextView tvCategory;

        public LinearLayout llCategory;
        public LinearLayout llProperty;

        public boolean isClickable = true;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPropertyTitle = itemView.findViewById(R.id.tv_settings_list_title);
            tvPropertyDesc = itemView.findViewById(R.id.tv_settings_list_desc);
            cbPropertyCheck = itemView.findViewById(R.id.cb_settings_list_check);
            tvCategory = itemView.findViewById(R.id.tv_settings_category);

            llCategory = itemView.findViewById(R.id.ll_settings_category);
            llProperty = itemView.findViewById(R.id.ll_settings_property);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            if (useFocusOutline) itemView.setBackgroundResource(R.drawable.focus_outline);
        }

        @Override
        public void onClick(View view) {
            if (!isClickable) return;
            int idx = manager.getPosition(view);
            if (clickListener != null) clickListener.onSettingsClick(idx, settings[idx].key);
        }

        @Override
        public boolean onLongClick(View view) {
            if (!isClickable) return false;
            int idx = manager.getPosition(view);
            if (longClickListener != null) longClickListener.onSettingsLongClick(idx, settings[idx].key);
            return true;
        }
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_settings_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (settings[position].type == MollaSetting.TYPE_CATEGORY) {
            holder.itemView.setClickable(false);
            holder.itemView.setFocusable(false);
            holder.llCategory.setVisibility(View.VISIBLE);
            holder.llProperty.setVisibility(View.GONE);
            holder.tvCategory.setText(settings[position].title);
            holder.isClickable = false;
        } else {
            holder.itemView.setClickable(!settings[position].goofy || (showGoofy));
            holder.itemView.setFocusable(!settings[position].goofy || (showGoofy));
            holder.llCategory.setVisibility(View.GONE);
            holder.llProperty.setVisibility(settings[position].goofy ? (showGoofy ? View.VISIBLE : View.GONE) : View.VISIBLE);
            holder.tvPropertyTitle.setText(settings[position].title);
            holder.tvPropertyDesc.setText(settings[position].desc);
            if (settings[position].type == MollaSetting.TYPE_CHECKBOX) {
                holder.cbPropertyCheck.setVisibility(View.VISIBLE);
                holder.cbPropertyCheck.setChecked(settings[position].value == 1);
            } else {
                holder.cbPropertyCheck.setVisibility(View.GONE);
            }
            holder.isClickable = true;
        }
    }

    @Override
    public int getItemCount() {
        return settings.length;
    }

    public void setOnSettingsClickedListener(SettingsClickedListener listener) {
        this.clickListener = listener;
    }

    public void setOnSettingsLongClickedListener(SettingsLongClickedListener listener) {
        this.longClickListener = listener;
    }

    public interface SettingsClickedListener {
        void onSettingsClick(int position, String key);
    }

    public interface SettingsLongClickedListener {
        void onSettingsLongClick(int position, String key);
    }
}
