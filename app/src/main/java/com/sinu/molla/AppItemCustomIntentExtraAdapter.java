// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;

public class AppItemCustomIntentExtraAdapter extends RecyclerView.Adapter<AppItemCustomIntentExtraAdapter.ViewHolder> {
    private final Context context;
    public final ArrayList<AppItemCustomIntentExtra> list;
    private final DataValidityChangedListener dvc;

    public AppItemCustomIntentExtraAdapter(Context context, ArrayList<AppItemCustomIntentExtra> list, DataValidityChangedListener dvc) {
        this.context = context;
        this.list = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            this.list.add(new AppItemCustomIntentExtra(list.get(i).getName(), list.get(i).getValueAs(list.get(i).getValueType())));
        }
        this.dvc = dvc;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final Button btnName;
        public final Button btnType;
        public final Button btnValue;
        public final CheckBox cbValue;
        public final ImageView ivDelete;

        public ViewHolder(@NonNull View v) {
            super(v);

            btnName = v.findViewById(R.id.btn_custom_item_extra_name);
            btnType = v.findViewById(R.id.btn_custom_item_extra_type);
            btnValue = v.findViewById(R.id.btn_custom_item_extra_value);
            cbValue = v.findViewById(R.id.cb_custom_item_extra_value);
            ivDelete = v.findViewById(R.id.iv_custom_item_extra_delete);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_custom_item_extra, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.btnName.setText(list.get(position).getName().isBlank() ? context.getString(R.string.dialog_custom_item_extras_name) : list.get(position).getName());
        holder.btnName.setOnClickListener((v) -> {
            EditText edtNewName = new EditText(context);
            edtNewName.setInputType(InputType.TYPE_CLASS_TEXT);
            AlertDialog adSetName = new AlertDialog.Builder(context)
                    .setView(edtNewName)
                    .setPositiveButton(R.string.dialog_custom_item_save, (d, i) -> {
                        list.get(position).setName(edtNewName.getText().toString().trim());
                        holder.btnName.setText(list.get(position).getName().isBlank() ? context.getString(R.string.dialog_custom_item_extras_name) : list.get(position).getName());
                        dvc.onDataValidityChanged();
                    })
                    .setNegativeButton(R.string.common_cancel, (d, i) -> {})
                    .create();
            adSetName.show();
            edtNewName.setText(list.get(position).getName());
        });
        holder.btnType.setText(typeToString(list.get(position).getValueType()));
        holder.btnType.setOnClickListener((v) -> {
            var nextType = getNextType(list.get(position).getValueType());
            if (nextType == String.class) list.get(position).setValue("");
            else if (nextType == Integer.class) list.get(position).setValue(0);
            else if (nextType == Long.class) list.get(position).setValue(0L);
            else if (nextType == Float.class) list.get(position).setValue(0f);
            else if (nextType == Double.class) list.get(position).setValue(0d);
            else if (nextType == Boolean.class) list.get(position).setValue(false);
            notifyItemChanged(position);
        });
        holder.cbValue.setOnCheckedChangeListener(null);
        if (list.get(position).getValueType() != Boolean.class) {
            holder.btnValue.setVisibility(View.VISIBLE);
            holder.cbValue.setVisibility(View.GONE);
            var currentValueString = list.get(position).getValueAsString();
            holder.btnValue.setText(currentValueString.isBlank() ? context.getString(R.string.dialog_custom_item_extras_value) : currentValueString);
        } else {
            holder.btnValue.setVisibility(View.GONE);
            holder.cbValue.setVisibility(View.VISIBLE);
            holder.cbValue.setChecked(list.get(position).getValueAs(Boolean.class));
        }
        holder.btnValue.setOnClickListener((v) -> {
            EditText edtNewValue = new EditText(context);
            edtNewValue.setInputType(InputType.TYPE_CLASS_TEXT);
            AlertDialog adSetValue = new AlertDialog.Builder(context)
                    .setView(edtNewValue)
                    .setPositiveButton(R.string.dialog_custom_item_save, (d, i) -> {
                        var currentType = list.get(position).getValueType();
                        if (currentType == String.class) {
                            var newStringValue = edtNewValue.getText().toString().trim();
                            list.get(position).setValue(newStringValue);
                            holder.btnValue.setText(newStringValue.isBlank() ? context.getString(R.string.dialog_custom_item_extras_value) : newStringValue);
                        } else if (currentType == Integer.class) {
                            try {
                                int newValue = Integer.parseInt(edtNewValue.getText().toString());
                                list.get(position).setValue(newValue);
                            } catch (Exception ignored) { Toast.makeText(context, R.string.dialog_custom_item_error_invalid_extra_value, Toast.LENGTH_SHORT).show(); }
                            holder.btnValue.setText(list.get(position).getValueAs(Integer.class).toString());
                        } else if (currentType == Long.class) {
                            try {
                                long newValue = Long.parseLong(edtNewValue.getText().toString());
                                list.get(position).setValue(newValue);
                            } catch (Exception ignored) { Toast.makeText(context, R.string.dialog_custom_item_error_invalid_extra_value, Toast.LENGTH_SHORT).show(); }
                            holder.btnValue.setText(list.get(position).getValueAs(Long.class).toString());
                        } else if (currentType == Float.class) {
                            try {
                                float newValue = Float.parseFloat(edtNewValue.getText().toString());
                                list.get(position).setValue(newValue);
                            } catch (Exception ignored) { Toast.makeText(context, R.string.dialog_custom_item_error_invalid_extra_value, Toast.LENGTH_SHORT).show(); }
                            holder.btnValue.setText(list.get(position).getValueAs(Float.class).toString());
                        } else if (currentType == Double.class) {
                            try {
                                double newValue = Double.parseDouble(edtNewValue.getText().toString());
                                list.get(position).setValue(newValue);
                            } catch (Exception ignored) { Toast.makeText(context, R.string.dialog_custom_item_error_invalid_extra_value, Toast.LENGTH_SHORT).show(); }
                            holder.btnValue.setText(list.get(position).getValueAs(Double.class).toString());
                        }
                    })
                    .setNegativeButton(R.string.common_cancel, (d, i) -> {})
                    .create();
            adSetValue.show();
            edtNewValue.setText(list.get(position).getValueAsString());
        });
        holder.cbValue.setOnCheckedChangeListener((v, checked) -> {
            list.get(position).setValue(checked);
        });
        holder.ivDelete.setOnClickListener((v) -> {
            list.remove(position);
            notifyDataSetChanged();
            dvc.onDataValidityChanged();
        });
        holder.ivDelete.setOnFocusChangeListener((view, hasFocus) -> {
            holder.ivDelete.setBackgroundColor(context.getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });
    }

    public boolean areNamesValid() {
        HashSet<String> names = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            var name = list.get(i).getName();
            if (name.isBlank()) return false;
            if (names.contains(name)) return false;
            names.add(name);
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String typeToString(Class<?> typeClass) {
        if (typeClass == String.class) return "String";
        if (typeClass == Integer.class) return "Integer";
        if (typeClass == Long.class) return "Long";
        if (typeClass == Float.class) return "Float";
        if (typeClass == Double.class) return "Double";
        if (typeClass == Boolean.class) return "Boolean";
        return "?";
    }

    private Class<?> getNextType(Class<?> currentType) {
        if (currentType == String.class) return Integer.class;
        if (currentType == Integer.class) return Long.class;
        if (currentType == Long.class) return Float.class;
        if (currentType == Float.class) return Double.class;
        if (currentType == Double.class) return Boolean.class;
        if (currentType == Boolean.class) return String.class;
        return String.class;
    }

    public interface DataValidityChangedListener {
        public void onDataValidityChanged();
    }
}
