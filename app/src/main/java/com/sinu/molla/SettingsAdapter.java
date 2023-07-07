// Copyright 2022-2023 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
    Context context;
    SharedPreferences pref;
    RecyclerView.LayoutManager manager;

    // TODO: use more sane method
    public MollaSetting[] settings = new MollaSetting[3];

    private OnSettingsClickedListener listener;

    public SettingsAdapter(Context context, SharedPreferences pref, RecyclerView.LayoutManager manager) {
        this.context = context;
        this.pref = pref;
        this.manager = manager;

        settings[0] = new MollaSetting(
                context.getString(R.string.settings_wallpaper_title),
                context.getString(R.string.settings_wallpaper_desc),
                MollaSetting.TYPE_BUTTON, "wallpaper"
        );
        settings[1] = new MollaSetting(
                context.getString(R.string.settings_hide_non_tv_apps_title),
                context.getString(R.string.settings_hide_non_tv_apps_desc),
                MollaSetting.TYPE_CHECKBOX, "hide_non_tv"
        );
        settings[2] = new MollaSetting(
                String.format(context.getString(R.string.settings_about_title), BuildConfig.VERSION_NAME),
                context.getString(R.string.settings_about_desc),
                MollaSetting.TYPE_BUTTON, "about"
        );
        for (MollaSetting s : settings) s.fetch(pref);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvTitle;
        public TextView tvDesc;
        public CheckBox cbCheck;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_settings_list_title);
            tvDesc = itemView.findViewById(R.id.tv_settings_list_desc);
            cbCheck = itemView.findViewById(R.id.cb_settings_list_check);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int idx = manager.getPosition(view);
            if (listener != null) listener.onSettingsClick(idx, settings[idx].key);
        }
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_settings_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTitle.setText(settings[position].title);
        holder.tvDesc.setText(settings[position].desc);
        if (settings[position].type == 1) {
            holder.cbCheck.setVisibility(View.VISIBLE);
            holder.cbCheck.setChecked(settings[position].value == 1);
        } else {
            holder.cbCheck.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void setOnSettingsClickedListener(OnSettingsClickedListener listener) {
        this.listener = listener;
    }

    public interface OnSettingsClickedListener {
        void onSettingsClick(int position, String key);
    }
}
