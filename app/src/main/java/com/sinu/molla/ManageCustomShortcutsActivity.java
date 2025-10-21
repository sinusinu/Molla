package com.sinu.molla;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

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
    boolean useFocusOutline;

    SharedPreferences pref;

    Runnable rUpdateCustomShortcuts;

    View.OnClickListener itemEditListener;
    View.OnClickListener itemDeleteListener;

    ActivityResultLauncher<Intent> pickMedia;
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
        useFocusOutline = pref.getInt("use_focus_outline", 0) == 1;

        items = new ArrayList<>();

        pickMedia = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                var data = result.getData();
                if (data != null) {
                    var uri = data.getData();
                    if (activeCsd != null) activeCsd.setCustomBannerImage(uri);
                }
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvManageCustomShortcutsShortcuts.setLayoutManager(manager);

        itemEditListener = (v) -> {
            var i = manager.getPosition(v);
            var item = items.get(i);
            activeCsd = new CustomShortcutDialog(this, simple, item, (newItem) -> {
                ((MollaApplication)getApplication()).getCustomShortcutManager().replaceCustomShortcutAt(i, newItem);
                rUpdateCustomShortcuts.run();
            });
            activeCsd.show();
        };
        itemDeleteListener = (v) -> {
            var i = manager.getPosition(v);
            File bannerFile = new File(getFilesDir(), items.get(i).customItemIdentifier + ".webp");
            //noinspection ResultOfMethodCallIgnored
            bannerFile.delete();
            ((MollaApplication)getApplication()).getCustomShortcutManager().removeCustomShortcutAt(i);
            rUpdateCustomShortcuts.run();
        };

        rUpdateCustomShortcuts = () -> {
            var cs = ((MollaApplication)getApplication()).getCustomShortcutManager().getCustomShortcuts();
            items.clear();
            items.addAll(cs);

            adapter = new AppItemListManageCustomAdapter(getApplicationContext(), this, items, itemEditListener, itemDeleteListener, simple, useFocusOutline);
            binding.rvManageCustomShortcutsShortcuts.setAdapter(adapter);

            binding.rvManageCustomShortcutsShortcuts.setVisibility(View.VISIBLE);
            binding.pbrManageCustomShortcutsLoading.setVisibility(View.GONE);

            adapter.notifyDataSetChanged();
        };

        rUpdateCustomShortcuts.run();

        var useFocusOutline = pref.getInt("use_focus_outline", 0) == 1;
        binding.ivManageCustomShortcutsBack.setBackgroundResource(useFocusOutline ? R.drawable.focus_outline : R.drawable.focus_highlight);
        binding.ivManageCustomShortcutsAdd.setBackgroundResource(useFocusOutline ? R.drawable.focus_outline : R.drawable.focus_highlight);

        binding.ivManageCustomShortcutsBack.setOnClickListener((v) -> {
            finish();
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        });

        binding.ivManageCustomShortcutsAdd.setOnClickListener((v) -> {
            activeCsd = new CustomShortcutDialog(this, simple, null, (newItem) -> {
                ((MollaApplication)getApplication()).getCustomShortcutManager().addCustomShortcut(newItem);
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
        Intent intentPickMedia = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intentPickMedia.addCategory(Intent.CATEGORY_OPENABLE);
        intentPickMedia.setType("image/*");
        pickMedia.launch(intentPickMedia);
    }

    private interface AppItemCustomizationFinishedListener {
        void onAppItemCustomizationFinished(AppItem newItem);
    }

    private class CustomShortcutDialog {
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

        AlertDialog adSetCustomActivity;
        View viewSetCustomActivity;
        ArrayList<String> activityList;

        AlertDialog adSetCustomIntentExtras;
        View viewSetCustomIntentExtras;
        LinearLayoutManager customIntentExtrasLayoutManager;
        AppItemCustomIntentExtraAdapter customIntentExtrasAdapter;

        final AppItem editingItem;
        String uuid;
        AppItem targetApp;
        String customTitle;
        AppItemIcon targetAppIcon;
        Drawable customBanner;
        String customActivity;
        ArrayList<AppItemCustomIntentExtra> customIntentExtras;

        AppItemCustomizationFinishedListener finishedListener;

        @SuppressLint({"InflateParams", "NotifyDataSetChanged"})
        public CustomShortcutDialog(ManageCustomShortcutsActivity activity, boolean simple, AppItem editingItem, AppItemCustomizationFinishedListener finishedListener) {
            this.activity = activity;
            this.simple = simple;
            appList = new ArrayList<>();
            uuid = UUID.randomUUID().toString();
            this.editingItem = editingItem;
            this.finishedListener = finishedListener;
            customIntentExtras = new ArrayList<>();

            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_custom_item, null, false);
            dialogView.findViewById(R.id.iv_dialog_custom_item_app_select).setOnClickListener((v) -> {
                // select target app
                dialogAppListView = activity.getLayoutInflater().inflate(R.layout.dialog_custom_item_app_list, null, false);
                appListLayoutManager = new LinearLayoutManager(activity);
                appListAdapter = new AppItemSingleSelectAdapter(activity.getApplicationContext(), appList, (vv) -> {
                    // set selected app
                    setTargetApp(appList.get(appListLayoutManager.getPosition(vv)));
                    adAppList.dismiss();
                }, useFocusOutline);
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
                        })
                        .setNegativeButton(R.string.common_cancel, (d, i) -> {})
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
                customBanner = null;
            });
            ((CheckBox)dialogView.findViewById(R.id.cb_dialog_custom_item_show_advanced)).setOnCheckedChangeListener((b, v) -> {
                dialogView.findViewById(R.id.ll_dialog_custom_item_advanced).setVisibility(v ? View.VISIBLE : View.GONE);
                Objects.requireNonNull(alertDialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            });
            dialogView.findViewById(R.id.iv_dialog_custom_item_activity_edit).setOnClickListener((v) -> {
                // set activity
                if (targetApp == null) return;
                activityList = new ArrayList<>();
                try {
                    var pm = activity.getPackageManager();
                    var pi = pm.getPackageInfo(targetApp.packageName, PackageManager.GET_ACTIVITIES);
                    if (pi.activities == null) return;
                    for (var a : pi.activities) {
                        if (a.exported && a.name.startsWith(targetApp.packageName)) activityList.add(a.name);
                    }
                } catch (Exception ignored) { return; }
                Collections.sort(activityList);
                activityList.add(0, activity.getString(R.string.dialog_custom_item_activity_list_default));
                viewSetCustomActivity = activity.getLayoutInflater().inflate(R.layout.dialog_custom_item_activity_list, null, false);
                adSetCustomActivity = new AlertDialog.Builder(activity)
                        .setCustomTitle(viewSetCustomActivity)
                        .setItems(activityList.toArray(new String[0]), (d, i) -> {
                            if (i == 0) {
                                customActivity = null;
                                ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_activity_value)).setText(R.string.dialog_custom_item_default);
                            } else {
                                customActivity = activityList.get(i);
                                ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_activity_value)).setText(customActivity);
                            }
                        })
                        .create();
                adSetCustomActivity.show();
            });
            dialogView.findViewById(R.id.iv_dialog_custom_item_extras_edit).setOnClickListener((v) -> {
                // set intent extras
                viewSetCustomIntentExtras = activity.getLayoutInflater().inflate(R.layout.dialog_custom_item_extras_list, null, false);
                customIntentExtrasLayoutManager = new LinearLayoutManager(activity);
                customIntentExtrasAdapter = new AppItemCustomIntentExtraAdapter(activity, customIntentExtras, () -> {
                    adSetCustomIntentExtras.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(customIntentExtrasAdapter.areNamesValid());
                }, useFocusOutline);
                ((RecyclerView)viewSetCustomIntentExtras.findViewById(R.id.rv_dialog_custom_item_extras_list_list)).setLayoutManager(customIntentExtrasLayoutManager);
                ((RecyclerView)viewSetCustomIntentExtras.findViewById(R.id.rv_dialog_custom_item_extras_list_list)).setAdapter(customIntentExtrasAdapter);
                ((SimpleItemAnimator)(Objects.requireNonNull(((RecyclerView)viewSetCustomIntentExtras.findViewById(R.id.rv_dialog_custom_item_extras_list_list)).getItemAnimator()))).setSupportsChangeAnimations(false);
                var ivExtrasListAdd = viewSetCustomIntentExtras.findViewById(R.id.iv_dialog_custom_item_extras_list_add);
                ivExtrasListAdd.setOnClickListener((vv) -> {
                    // add new intent extra
                    customIntentExtrasAdapter.list.add(new AppItemCustomIntentExtra("", ""));
                    customIntentExtrasAdapter.notifyDataSetChanged();
                    adSetCustomIntentExtras.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(customIntentExtrasAdapter.areNamesValid());
                });
                if (useFocusOutline) ivExtrasListAdd.setBackgroundResource(R.drawable.focus_outline);
                adSetCustomIntentExtras = new AlertDialog.Builder(activity)
                        .setView(viewSetCustomIntentExtras)
                        .setPositiveButton(R.string.dialog_custom_item_save, (d, i) -> {
                            customIntentExtras.clear();
                            customIntentExtras.addAll(customIntentExtrasAdapter.list);
                            int extrasCount = customIntentExtras.size();
                            ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_extras_value)).setText(activity.getResources().getQuantityString(R.plurals.dialog_custom_item_extras_count, extrasCount, extrasCount));
                        })
                        .setNegativeButton(R.string.common_cancel, (d, i) -> {})
                        .create();
                adSetCustomIntentExtras.show();
            });
            ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(simple ? AppCompatResources.getDrawable(activity, R.drawable.generic_simple) : AppCompatResources.getDrawable(activity, R.drawable.generic));
            int[] focusableViews = {
                    R.id.iv_dialog_custom_item_app_select,
                    R.id.iv_dialog_custom_item_title_edit,
                    R.id.iv_dialog_custom_item_banner_add,
                    R.id.iv_dialog_custom_item_banner_clear,
                    R.id.cb_dialog_custom_item_show_advanced,
                    R.id.iv_dialog_custom_item_activity_edit,
                    R.id.iv_dialog_custom_item_extras_edit,
            };
            for (int v : focusableViews) dialogView.findViewById(v).setBackgroundResource(useFocusOutline ? R.drawable.focus_outline : R.drawable.focus_highlight);

            alertDialog = new AlertDialog.Builder(activity)
                    .setView(dialogView)
                    .setPositiveButton(R.string.dialog_custom_item_save, (d, i) -> {
                        // save new custom item
                        if (customBanner != null) {
                            File bannerFileTemp = new File(activity.getFilesDir(), uuid + ".temp.webp");
                            File bannerFile = new File(activity.getFilesDir(), uuid + ".webp");
                            if (bannerFileTemp.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                bannerFileTemp.renameTo(bannerFile);
                            }
                        } else if (editingItem != null) {
                            File bannerFile = new File(activity.getFilesDir(), uuid + ".webp");
                            if (bannerFile.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                bannerFile.delete();
                            }
                        }
                        AppItem newItem = new AppItem(
                                uuid,
                                targetApp.displayName,
                                customTitle,
                                targetApp.packageName,
                                targetApp.activityName,
                                customActivity,
                                customIntentExtras
                        );
                        finishedListener.onAppItemCustomizationFinished(newItem);
                    })
                    .setNegativeButton(R.string.common_cancel, (d, i) -> {})
                    .setOnDismissListener((d) -> {
                        File bannerFileTemp = new File(activity.getFilesDir(), uuid + ".temp.webp");
                        //noinspection ResultOfMethodCallIgnored
                        bannerFileTemp.delete();
                        activity.activeCsd = null;
                    })
                    .create();

            if (editingItem != null && editingItem.isCustomItem) {
                uuid = editingItem.customItemIdentifier;
                targetApp = editingItem;
                targetAppIcon = AppItemIcon.getAppItemIcon((MollaApplication)activity.getApplicationContext(), editingItem, false);
                customTitle = targetApp.customItemDisplayName;
                customActivity = targetApp.customItemActivityName;
                customIntentExtras = targetApp.customItemIntentExtras;
                File bannerFile = new File(activity.getFilesDir(), uuid + ".webp");
                if (bannerFile.exists()) customBanner = Drawable.createFromPath(bannerFile.getAbsolutePath());
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
                if (customActivity != null) {
                    ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_activity_value)).setText(customActivity);
                } else {
                    ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_activity_value)).setText(R.string.dialog_custom_item_default);
                }
                ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_title)).setText(R.string.dialog_custom_item_title_edit);
            }
        }

        public void show() {
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            int extrasCount = customIntentExtras.size();
            ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_extras_value)).setText(activity.getResources().getQuantityString(R.plurals.dialog_custom_item_extras_count, extrasCount, extrasCount));
            if (editingItem != null && editingItem.isCustomItem) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }

        public void setTargetApp(AppItem appItem) {
            boolean isNewApp = targetApp == null || !targetApp.packageName.equals(appItem.packageName);
            targetApp = appItem;
            targetAppIcon = AppItemIcon.getAppItemIcon((MollaApplication)activity.getApplicationContext(), appItem);
            ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_app_name_value)).setText(targetApp.displayName);
            ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_title_value)).setText((customTitle == null) ? targetApp.displayName : customTitle);
            if (customBanner != null) {
                ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(customBanner);
                ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(null);
            } else {
                if (targetAppIcon.type == AppItemIcon.IconType.LEANBACK) {
                    ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(targetAppIcon.drawable);
                    ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(null);
                } else {
                    ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_banner)).setImageDrawable(simple ? AppCompatResources.getDrawable(activity, R.drawable.generic_simple) : AppCompatResources.getDrawable(activity, R.drawable.generic));
                    ((ImageView)dialogView.findViewById(R.id.iv_dialog_custom_item_icon)).setImageDrawable(targetAppIcon.drawable);
                }
            }
            if (isNewApp) {
                customActivity = null;
                ((TextView)dialogView.findViewById(R.id.tv_dialog_custom_item_activity_value)).setText(R.string.dialog_custom_item_default);
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