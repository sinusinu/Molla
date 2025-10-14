package com.sinu.molla;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sinu.molla.databinding.ActivityManageCustomShortcutsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class ManageCustomShortcutsActivity extends AppCompatActivity {
    ActivityManageCustomShortcutsBinding binding;

    ArrayList<AppItem> items;
    AppItemListManageCustomAdapter adapter;

    boolean simple;

    SharedPreferences pref;

    Runnable rUpdateCustomShortcuts;

    View.OnClickListener itemEditListener;
    View.OnClickListener itemDeleteListener;

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    CustomShortcutDialog activeCsd = null;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityManageCustomShortcutsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);
        simple = pref.getInt("simple_icon_bg", 0) == 1;

        items = new ArrayList<>();

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), (uri) -> {
            if (uri != null) {
                if (activeCsd != null) activeCsd.setCustomBannerImage(uri);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvManageCustomShortcutsShortcuts.setLayoutManager(manager);

        itemEditListener = (v) -> {
            var i = manager.getPosition(v);
            var item = items.get(i);
            activeCsd = new CustomShortcutDialog(this, simple, item, (newItem) -> {
                ((MollaApplication)getApplication()).getCustomItemManager().replaceCustomShortcutAt(i, newItem);
                rUpdateCustomShortcuts.run();
            });
            activeCsd.show();
        };
        itemDeleteListener = (v) -> {
            var i = manager.getPosition(v);
            File bannerFile = new File(getFilesDir(), items.get(i).customItemIdentifier + ".webp");
            //noinspection ResultOfMethodCallIgnored
            bannerFile.delete();
            ((MollaApplication)getApplication()).getCustomItemManager().removeCustomShortcutAt(i);
            rUpdateCustomShortcuts.run();
        };

        rUpdateCustomShortcuts = () -> {
            var cs = ((MollaApplication)getApplication()).getCustomItemManager().getCustomShortcuts();
            items.clear();
            items.addAll(cs);

            adapter = new AppItemListManageCustomAdapter(getApplicationContext(), this, items, itemEditListener, itemDeleteListener, simple);
            binding.rvManageCustomShortcutsShortcuts.setAdapter(adapter);

            binding.rvManageCustomShortcutsShortcuts.setVisibility(View.VISIBLE);
            binding.pbrManageCustomShortcutsLoading.setVisibility(View.GONE);

            adapter.notifyDataSetChanged();
        };

        rUpdateCustomShortcuts.run();

        binding.ivManageCustomShortcutsBack.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivManageCustomShortcutsBack.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivManageCustomShortcutsBack.setOnClickListener((v) -> {
            finish();
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        });

        binding.ivManageCustomShortcutsAdd.setOnFocusChangeListener((view, hasFocus) -> {
            binding.ivManageCustomShortcutsAdd.setBackgroundColor(getColor(hasFocus ? R.color.transparent_white : R.color.transparent));
        });

        binding.ivManageCustomShortcutsAdd.setOnClickListener((v) -> {
            activeCsd = new CustomShortcutDialog(this, simple, null, (newItem) -> {
                ((MollaApplication)getApplication()).getCustomItemManager().addCustomShortcut(newItem);
                rUpdateCustomShortcuts.run();
            });
            activeCsd.show();
        });

        // hide system bars
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        var backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onResume() {
        super.onResume();

        String orient = pref.getString("forced_orientation", "disable");
        if ("landscape".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        else if ("portrait".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        ((MollaApplication)getApplication()).getWallpaperCache().setWallpaperOnImageView(binding.ivManageCustomShortcutsWallpaper, false);
    }

    private void callBannerImagePicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private interface AppItemCustomizationFinishedListener {
        void onAppItemCustomizationFinished(AppItem newItem);
    }

    private static class CustomShortcutDialog {
        ManageCustomShortcutsActivity activity;
        boolean simple;

        AlertDialog alertDialog;
        View dialogView;

        AlertDialog adAppList;
        View dialogAppListView;
        LinearLayoutManager appListLayoutManager;
        AppItemSingleSelectAdapter appListAdapter;
        ArrayList<AppItem> appList;

        AlertDialog adSetCustomTitle;
        View viewSetCustomTitleTitle;
        EditText edtCustomTitle;

        final AppItem editingItem;
        String uuid;
        AppItem targetApp;
        String customTitle;
        AppItemIcon targetAppIcon;
        Drawable customBanner;

        AppItemCustomizationFinishedListener finishedListener;

        @SuppressLint({"InflateParams", "NotifyDataSetChanged"})
        public CustomShortcutDialog(ManageCustomShortcutsActivity activity, boolean simple, AppItem editingItem, AppItemCustomizationFinishedListener finishedListener) {
            this.activity = activity;
            this.simple = simple;
            appList = new ArrayList<>();
            uuid = UUID.randomUUID().toString();
            this.editingItem = editingItem;
            this.finishedListener = finishedListener;

            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_custom_item, null, false);
            dialogView.findViewById(R.id.iv_dialog_custom_item_app_select).setOnClickListener((v) -> {
                // select target app
                dialogAppListView = activity.getLayoutInflater().inflate(R.layout.dialog_custom_item_app_list, null, false);
                appListLayoutManager = new LinearLayoutManager(activity);
                appListAdapter = new AppItemSingleSelectAdapter(activity.getApplicationContext(), appList, (vv) -> {
                    // set selected app
                    setTargetApp(appList.get(appListLayoutManager.getPosition(vv)));
                    adAppList.dismiss();
                }, simple);
                appListAdapter.setSelectedItem(targetApp);
                ((RecyclerView)dialogAppListView.findViewById(R.id.rv_dialog_custom_item_app_list_list)).setLayoutManager(appListLayoutManager);
                ((RecyclerView)dialogAppListView.findViewById(R.id.rv_dialog_custom_item_app_list_list)).setAdapter(appListAdapter);
                adAppList = new AlertDialog.Builder(activity)
                        .setView(dialogAppListView)
                        .create();
                adAppList.show();
                new Thread(() -> {
                    AppItem.fetchAllAppsAsync(activity.getApplicationContext(), (items) -> {
                        Collections.sort(items, AppItem::compareByDisplayName);
                        appList.clear();
                        appList.addAll(items);

                        activity.runOnUiThread(() -> {
                            dialogAppListView.findViewById(R.id.pbr_dialog_custom_item_app_list_loading).setVisibility(View.GONE);
                            dialogAppListView.findViewById(R.id.rv_dialog_custom_item_app_list_list).setVisibility(View.VISIBLE);

                            appListAdapter.notifyDataSetChanged();
                            Objects.requireNonNull(adAppList.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        });
                    });
                }).start();
            });
            dialogView.findViewById(R.id.iv_dialog_custom_item_title_edit).setOnClickListener((v) -> {
                // edit item title
                viewSetCustomTitleTitle = activity.getLayoutInflater().inflate(R.layout.dialog_custom_item_title, null, false);
                edtCustomTitle = new EditText(activity);
                edtCustomTitle.setText(customTitle);
                edtCustomTitle.setMaxLines(1);
                edtCustomTitle.setInputType(InputType.TYPE_CLASS_TEXT);
                adSetCustomTitle = new AlertDialog.Builder(activity)
                        .setCustomTitle(viewSetCustomTitleTitle)
                        .setView(edtCustomTitle)
                        .setPositiveButton(R.string.common_ok, (d, i) -> {
                            // save item title
                            customTitle = edtCustomTitle.getText().toString();
                            if (customTitle.isBlank()) customTitle = null;
                            ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_title_value)).setText((customTitle == null) ? (targetApp == null ? activity.getString(R.string.dialog_custom_item_not_set) : targetApp.displayName) : customTitle);
                            d.dismiss();
                        })
                        .setNegativeButton(R.string.common_cancel, (d, i) -> d.dismiss())
                        .create();
                adSetCustomTitle.show();
            });
            dialogView.findViewById(R.id.iv_dialog_custom_item_banner_add).setOnClickListener((v) -> {
                // add custom banner
                activity.runOnUiThread(activity::callBannerImagePicker);
            });
            dialogView.findViewById(R.id.iv_dialog_custom_item_banner_clear).setOnClickListener((v) -> {
                // clear custom banner
                if (targetAppIcon == null) {
                    ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(simple ? AppCompatResources.getDrawable(activity, R.drawable.generic_simple) : AppCompatResources.getDrawable(activity, R.drawable.generic));
                    ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(AppCompatResources.getDrawable(activity, R.drawable.ic_apps));
                } else {
                    if (targetAppIcon.type == AppItemIcon.IconType.LEANBACK) {
                        ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(targetAppIcon.drawable);
                        ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(null);
                    } else {
                        ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(simple ? AppCompatResources.getDrawable(activity, R.drawable.generic_simple) : AppCompatResources.getDrawable(activity, R.drawable.generic));
                        ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(targetAppIcon.drawable);
                    }
                }
                File bannerFileTemp = new File(activity.getFilesDir(), uuid + ".temp.webp");
                //noinspection ResultOfMethodCallIgnored
                bannerFileTemp.delete();
                customBanner = null;
            });
            ((CheckBox)dialogView.findViewById(R.id.cb_dialog_custom_item_show_advanced)).setOnCheckedChangeListener((b, v) -> {
                dialogView.findViewById(R.id.ll_dialog_custom_item_advanced).setVisibility(v ? View.VISIBLE : View.GONE);
                Objects.requireNonNull(alertDialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            });

            alertDialog = new AlertDialog.Builder(activity)
                    .setView(dialogView)
                    .setPositiveButton(R.string.dialog_custom_item_save, (d, i) -> {
                        // save new custom item
                        File bannerFileTemp = new File(activity.getFilesDir(), uuid + ".temp.webp");
                        File bannerFile = new File(activity.getFilesDir(), uuid + ".webp");
                        if (bannerFileTemp.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            bannerFileTemp.renameTo(bannerFile);
                        }
                        AppItem newItem = new AppItem(
                                uuid,
                                targetApp.displayName,
                                customTitle,
                                targetApp.packageName,
                                targetApp.activityName,
                                null
                        );
                        finishedListener.onAppItemCustomizationFinished(newItem);
                        d.dismiss();
                    })
                    .setNegativeButton(R.string.common_cancel, (d, i) -> {
                        File bannerFileTemp = new File(activity.getFilesDir(), uuid + ".temp.webp");
                        //noinspection ResultOfMethodCallIgnored
                        bannerFileTemp.delete();
                        d.dismiss();
                    })
                    .setOnDismissListener((d) -> activity.activeCsd = null)
                    .create();

            if (editingItem != null && editingItem.isCustomItem) {
                uuid = editingItem.customItemIdentifier;
                targetApp = editingItem;
                targetAppIcon = AppItemIcon.getAppItemIcon((MollaApplication)activity.getApplicationContext(), editingItem);
                customTitle = targetApp.customItemDisplayName;
                ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_app_name_value)).setText(targetApp.displayName);
                ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_title_value)).setText((customTitle == null) ? targetApp.displayName : customTitle);
                if (customBanner != null) {
                    ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(customBanner);
                    ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(null);
                } else {
                    if (targetAppIcon.type == AppItemIcon.IconType.LEANBACK) {
                        ((ImageView) dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(targetAppIcon.drawable);
                        ((ImageView) dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(null);
                    } else {
                        ((ImageView) dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(simple ? AppCompatResources.getDrawable(activity, R.drawable.generic_simple) : AppCompatResources.getDrawable(activity, R.drawable.generic));
                        ((ImageView) dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(targetAppIcon.drawable);
                    }
                }
            }
        }

        public void show() {
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            if (editingItem != null && editingItem.isCustomItem) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }

        public void setTargetApp(AppItem appItem) {
            targetApp = appItem;
            targetAppIcon = AppItemIcon.getAppItemIcon((MollaApplication)activity.getApplicationContext(), appItem);
            ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_app_name_value)).setText(targetApp.displayName);
            ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_title_value)).setText((customTitle == null) ? targetApp.displayName : customTitle);
            if (customBanner != null) {
                ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(customBanner);
                ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(null);
            } else {
                if (targetAppIcon.type == AppItemIcon.IconType.LEANBACK) {
                    ((ImageView) dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(targetAppIcon.drawable);
                    ((ImageView) dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(null);
                } else {
                    ((ImageView) dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(simple ? AppCompatResources.getDrawable(activity, R.drawable.generic_simple) : AppCompatResources.getDrawable(activity, R.drawable.generic));
                    ((ImageView) dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(targetAppIcon.drawable);
                }
            }
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }

        /** @noinspection ResultOfMethodCallIgnored*/
        public void setCustomBannerImage(Uri uri) {
            Bitmap bitmap = null;
            try (InputStream input = activity.getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                Toast.makeText(activity, R.string.dialog_custom_item_error_invalid_banner, Toast.LENGTH_SHORT).show();
                return;
            }

            File copyFile = new File(activity.getFilesDir(), uuid + ".temp.webp");
            try (FileOutputStream fos = new FileOutputStream(copyFile)) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 90, fos);
            } catch (Exception e) {
                Toast.makeText(activity, R.string.dialog_custom_item_error_invalid_banner, Toast.LENGTH_SHORT).show();
                copyFile.delete();
                return;
            }
            bitmap.recycle();
            bitmap = null;

            customBanner = Drawable.createFromPath(copyFile.getAbsolutePath());
            ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(customBanner);
            ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(null);
        }
    }
}