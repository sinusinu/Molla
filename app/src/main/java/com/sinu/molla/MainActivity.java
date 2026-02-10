// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sinu.molla.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    SharedPreferences pref;
    boolean isCloseable = false;

    ArrayList<AppItem> items;
    AppItemAdapter adapter;

    private Animation animScaleUp;
    private Animation animScaleDown;

    Handler h;
    Runnable rUpdateStatus;
    Runnable rCountdownAutoLaunch;

    BatteryManager bm;
    boolean batteryExist;
    ConnectivityManager cm;
    WifiManager wm;
    BluetoothManager bt;

    public boolean isFavListUpdateReserved = true;

    AppItem autoLaunchTarget = null;
    boolean isWaitingForAutoLaunch = false;
    int autoLaunchCountdown = 0;

    boolean useFocusOutline;

    boolean kioskModeActive;
    StringBuilder sbPin;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = getSharedPreferences("com.sinu.molla.settings", Context.MODE_PRIVATE);
        useFocusOutline = pref.getInt("use_focus_outline", 0) == 1;

        items = new ArrayList<>();

        HorizontallyFocusedLinearLayoutManager manager = new HorizontallyFocusedLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        adapter = new AppItemAdapter(getApplicationContext(), this, manager, items, true, (pref.getInt("simple_icon_bg", 0) == 1));

        adapter.setOnAppItemFocusChangedListener((i, n) -> {
            binding.tvMainFavName.setText(n);
        });

        adapter.setOnKioskUnlockRequestedListener(() -> {
            if (kioskModeActive) {
                String kioskPin = pref.getString("kiosk_mode_pin", null);
                if (kioskPin == null) {
                    kioskModeActive = false;
                    adapter.setKioskMode(false);
                    binding.lvMainAll.setVisibility(View.VISIBLE);
                    binding.lvMainSettings.setVisibility(View.VISIBLE);
                    Toast.makeText(this, R.string.main_kiosk_unlocked, Toast.LENGTH_SHORT).show();
                } else {
                    if (sbPin == null) sbPin = new StringBuilder();
                    else sbPin.setLength(0);
                    var viewKioskPin = getLayoutInflater().inflate(R.layout.dialog_kiosk_pin, null, false);
                    ((TextView)viewKioskPin.findViewById(R.id.tv_dialog_kiosk_pin_title)).setText(R.string.dialog_kiosk_pin_title_unlock);
                    int[] buttons = { R.id.btn_dialog_kiosk_pin_0, R.id.btn_dialog_kiosk_pin_1, R.id.btn_dialog_kiosk_pin_2, R.id.btn_dialog_kiosk_pin_3, R.id.btn_dialog_kiosk_pin_4, R.id.btn_dialog_kiosk_pin_5, R.id.btn_dialog_kiosk_pin_6, R.id.btn_dialog_kiosk_pin_7, R.id.btn_dialog_kiosk_pin_8, R.id.btn_dialog_kiosk_pin_9 };
                    for (int j = 0; j < buttons.length; j++) {
                        int fi = j;
                        View v = viewKioskPin.findViewById(buttons[j]);
                        v.setOnClickListener((vv) -> {
                            if (sbPin.length() < 6) {
                                sbPin.append(fi);
                                ((TextView)viewKioskPin.findViewById(R.id.tv_dialog_kiosk_pin_display)).setText(sbPin.toString());
                            }
                        });
                        v.setOnKeyListener((view, code, event) -> false);
                        v.setBackgroundResource(useFocusOutline ? R.drawable.focus_outline : R.drawable.focus_highlight);
                    }
                    var ivBksp = viewKioskPin.findViewById(R.id.iv_dialog_kiosk_pin_bksp);
                    ivBksp.setOnClickListener((v) -> {
                        if (sbPin.length() > 0) {
                            sbPin.setLength(sbPin.length() - 1);
                            ((TextView)viewKioskPin.findViewById(R.id.tv_dialog_kiosk_pin_display)).setText(sbPin.toString());
                        }
                    });
                    ivBksp.setOnKeyListener((view, code, event) -> false);
                    ivBksp.setBackgroundResource(useFocusOutline ? R.drawable.focus_outline : R.drawable.focus_highlight);
                    var adKioskPin = new AlertDialog.Builder(this)
                            .setView(viewKioskPin)
                            .setPositiveButton(R.string.common_ok, null)
                            .setNegativeButton(R.string.common_cancel, null)
                            .setNeutralButton(R.string.dialog_kiosk_pin_help, null)
                            .create();
                    adKioskPin.setOnKeyListener((view, code, event) -> {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (code >= KeyEvent.KEYCODE_0 && code <= KeyEvent.KEYCODE_9) {
                                int number = code - KeyEvent.KEYCODE_0;
                                if (sbPin.length() < 6) {
                                    sbPin.append(number);
                                    ((TextView)viewKioskPin.findViewById(R.id.tv_dialog_kiosk_pin_display)).setText(sbPin.toString());
                                }
                                return true;
                            } else if (code == KeyEvent.KEYCODE_DEL) {
                                if (sbPin.length() > 0) {
                                    sbPin.setLength(sbPin.length() - 1);
                                    ((TextView)viewKioskPin.findViewById(R.id.tv_dialog_kiosk_pin_display)).setText(sbPin.toString());
                                }
                                return true;
                            }
                        }
                        return false;
                    });
                    adKioskPin.show();
                    adKioskPin.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener((v) -> {
                        if (kioskPin.equals(sbPin.toString())) {
                            kioskModeActive = false;
                            adapter.setKioskMode(false);
                            binding.lvMainAll.setVisibility(View.VISIBLE);
                            binding.lvMainSettings.setVisibility(View.VISIBLE);
                            Toast.makeText(this, R.string.main_kiosk_unlocked, Toast.LENGTH_SHORT).show();
                            adKioskPin.dismiss();
                        } else {
                            sbPin.setLength(0);
                            ((TextView)viewKioskPin.findViewById(R.id.tv_dialog_kiosk_pin_display)).setText(sbPin.toString());
                            Toast.makeText(this, R.string.dialog_kiosk_pin_unlock_mismatch, Toast.LENGTH_SHORT).show();
                        }
                    });
                    adKioskPin.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener((v) -> {
                        var adKioskHelp = new AlertDialog.Builder(this)
                                .setTitle(R.string.dialog_kiosk_pin_help_title)
                                .setMessage(R.string.dialog_kiosk_pin_help_content)
                                .setPositiveButton(R.string.common_ok, (dd, ii) -> {})
                                .create();
                        adKioskHelp.show();
                    });
                }
            }
        });

        binding.rvMainFav.setLayoutManager(manager);
        binding.rvMainFav.setAdapter(adapter);

        binding.lvMainAll.setOnClickListener((v) -> {
            startActivity(new Intent(this, AllAppsActivity.class));
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            isFavListUpdateReserved = true;
        });
        binding.lvMainSettings.setOnClickListener((v) -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            isFavListUpdateReserved = true;
        });

        animScaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        animScaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        animScaleUp.setFillAfter(true);

        binding.lvMainAll.setOnFocusChangeListener((view, hasFocus) -> {
            binding.lvMainAll.startAnimation(hasFocus ? animScaleUp : animScaleDown);
        });
        binding.lvMainSettings.setOnFocusChangeListener((view, hasFocus) -> {
            binding.lvMainSettings.startAnimation(hasFocus ? animScaleUp : animScaleDown);
        });

        // setup system bars (hide/show on onResume)
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.setAppearanceLightStatusBars(false);

        var batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryIntent == null) {
            batteryExist = false;
            bm = null;
        } else {
            int batteryStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            batteryExist = batteryStatus != BatteryManager.BATTERY_STATUS_UNKNOWN;
            bm = (BatteryManager)getSystemService(Context.BATTERY_SERVICE);
        }

        if (!batteryExist) {
            binding.ivMainBatIcon.setVisibility(View.GONE);
            binding.tvMainBatPercentage.setVisibility(View.GONE);
        } else {
            binding.ivMainBatIcon.setVisibility(View.VISIBLE);
            binding.tvMainBatPercentage.setVisibility(View.VISIBLE);
        }

        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        // must explicitly check this permission, might be missing on devices without wifi
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        } else {
            wm = null;
        }

        bt = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        rUpdateStatus = () -> {
            binding.ivMainBtIcon.setVisibility(View.VISIBLE);
            binding.ivMainConnIcon.setVisibility(View.VISIBLE);
            binding.ivMainBatIcon.setVisibility(View.VISIBLE);
            binding.tvMainBatPercentage.setVisibility(View.VISIBLE);
            binding.tvMainTime.setVisibility(View.VISIBLE);

            if (batteryExist && bm != null) {
                int batteryStatus = 0;
                int batteryPercent = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    batteryStatus = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);
                    batteryPercent = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                } else {
                    var bi = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    if (bi != null) {
                        batteryStatus = bi.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                        int level = bi.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = bi.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                        batteryPercent = (int)Math.ceil(level * 100 / (float)scale);
                    }
                }

                binding.ivMainBatIcon.setVisibility(View.VISIBLE);
                binding.tvMainBatPercentage.setVisibility(View.VISIBLE);
                if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                    binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_charging));
                } else if (batteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
                    binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_full));
                } else {
                    if (batteryPercent > 95f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_full));
                    } else if (batteryPercent > 80f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_6));
                    } else if (batteryPercent > 68f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_5));
                    } else if (batteryPercent > 56f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_4));
                    } else if (batteryPercent > 44f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_3));
                    } else if (batteryPercent > 32f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_2));
                    } else if (batteryPercent > 20f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_1));
                    } else if (batteryPercent > 10f) {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_0));
                    } else {
                        binding.ivMainBatIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_battery_alert));
                    }
                }

                binding.tvMainBatPercentage.setText(String.format(getString(R.string.main_bat), batteryPercent));
            }

            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                binding.ivMainConnIcon.setVisibility(View.GONE);
            } else {
                binding.ivMainConnIcon.setVisibility(View.VISIBLE);
                switch (ni.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        // assuming wm is non-null; devices without wifi won't reach here, right?
                        WifiInfo info = wm.getConnectionInfo();
                        int level = WifiManager.calculateSignalLevel(info.getRssi(), 3);
                        switch (level) {
                            case 2:
                                binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_wifi));
                                break;
                            case 1:
                                binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_wifi_low));
                                break;
                            case 0:
                                binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_wifi_verylow));
                                break;
                        }
                         break;
                    case ConnectivityManager.TYPE_MOBILE:
                        binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_cellular_data));
                        break;
                    default:
                        binding.ivMainConnIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_conn_ethernet));
                        break;
                }

            }

            try {
                BluetoothAdapter ba = bt.getAdapter();
                binding.ivMainBtIcon.setVisibility(ba.isEnabled() ? View.VISIBLE : View.GONE);
            } catch (Exception ignored) {
                binding.ivMainBtIcon.setVisibility(View.GONE);
            }

            binding.tvMainTime.setText(DateFormat.getTimeFormat(this).format(new Date()));

            boolean showBt = pref.getBoolean("indicator_show_bt", true);
            boolean showNet = pref.getBoolean("indicator_show_net", true);
            boolean showBat = pref.getBoolean("indicator_show_bat", true);
            boolean showTime = pref.getBoolean("indicator_show_time", true);

            if (!showBt) binding.ivMainBtIcon.setVisibility(View.GONE);
            if (!showNet) binding.ivMainConnIcon.setVisibility(View.GONE);
            if (!showBat) { binding.ivMainBatIcon.setVisibility(View.GONE); binding.tvMainBatPercentage.setVisibility(View.GONE); }
            if (!showTime) binding.tvMainTime.setVisibility(View.GONE);

            h.postDelayed(rUpdateStatus, 2000);
        };

        rCountdownAutoLaunch = () -> {
            if (!isWaitingForAutoLaunch) return;
            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                isWaitingForAutoLaunch = false;
                setAutoLaunchOverlayVisibility(false);
                return;
            }
            autoLaunchCountdown--;
            if (autoLaunchCountdown == 0) {
                isWaitingForAutoLaunch = false;
                setAutoLaunchOverlayVisibility(false);
                AppItem.launch(this, autoLaunchTarget);
            } else {
                binding.tvMainAutolaunchOverlaySeconds.setText(autoLaunchCountdown+"");
                h.postDelayed(rCountdownAutoLaunch, 1000);
            }
        };

        h = new Handler(getMainLooper());

        var backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isWaitingForAutoLaunch) {
                    isWaitingForAutoLaunch = false;
                    setAutoLaunchOverlayVisibility(false);
                } else {
                    if (isCloseable) finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);

        // check if device is rebooted (alternative, not set as default launcher)
        if (pref.getInt("autolaunch_alt_detect", 0) == 1 && (getIntent().getBooleanExtra("BOOT_COMPLETE_RECEIVED", false) || pref.getInt("boot_complete_alt_detect_hint", 0) == 1)) {
            String autolaunchTargetPackage = pref.getString("autolaunch_package", null);
            if (autolaunchTargetPackage != null) {
                if (autolaunchTargetPackage.startsWith("custom:")) {
                    var autolaunchTargetCustomItemId = autolaunchTargetPackage.substring(7);
                    var customItems = ((MollaApplication)getApplication()).getCustomShortcutManager().getCustomShortcuts();
                    for (int i = 0; i < customItems.size(); i++) {
                        var customItem = customItems.get(i);
                        if (customItem.customItemIdentifier.equals(autolaunchTargetCustomItemId)) {
                            autoLaunchTarget = customItem;
                            if (!isWaitingForAutoLaunch) initiateAutoLaunch();
                            break;
                        }
                    }
                } else {
                    ArrayList<String> pnAutoLaunchTarget = new ArrayList<>();
                    pnAutoLaunchTarget.add(autolaunchTargetPackage);
                    AppItem.fetchListOfAppsAsync(this, pnAutoLaunchTarget, (items) -> {
                        if (items.size() == 1) runOnUiThread(() -> {
                            autoLaunchTarget = items.get(0);
                            if (!isWaitingForAutoLaunch) initiateAutoLaunch();
                        });
                    });
                }
            }
        }
        pref.edit().remove("boot_complete_alt_detect_hint").apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // check if device rebooted (alternative, set as default launcher)
        if (pref.getInt("autolaunch_alt_detect", 0) == 1 && intent.getBooleanExtra("BOOT_COMPLETE_RECEIVED", false)) {
            intent.removeExtra("BOOT_COMPLETED_RECEIVED");
            setIntent(intent);
            String autolaunchTargetPackage = pref.getString("autolaunch_package", null);
            if (autolaunchTargetPackage != null) {
                if (autolaunchTargetPackage.startsWith("custom:")) {
                    var autolaunchTargetCustomItemId = autolaunchTargetPackage.substring(7);
                    var customItems = ((MollaApplication)getApplication()).getCustomShortcutManager().getCustomShortcuts();
                    for (int i = 0; i < customItems.size(); i++) {
                        var customItem = customItems.get(i);
                        if (customItem.customItemIdentifier.equals(autolaunchTargetCustomItemId)) {
                            autoLaunchTarget = customItem;
                            if (!isWaitingForAutoLaunch) initiateAutoLaunch();
                            break;
                        }
                    }
                } else {
                    ArrayList<String> pnAutoLaunchTarget = new ArrayList<>();
                    pnAutoLaunchTarget.add(autolaunchTargetPackage);
                    AppItem.fetchListOfAppsAsync(this, pnAutoLaunchTarget, (items) -> {
                        if (items.size() == 1) runOnUiThread(() -> {
                            autoLaunchTarget = items.get(0);
                            if (!isWaitingForAutoLaunch) initiateAutoLaunch();
                        });
                    });
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFavListUpdateReserved) {
            binding.pbrMainLoading.setVisibility(View.INVISIBLE);
            binding.rvMainFav.setVisibility(View.INVISIBLE);
            binding.tvMainFavName.setVisibility(View.INVISIBLE);
        }
        h.removeCallbacks(rUpdateStatus);
    }

    @SuppressLint({"SourceLockedOrientationActivity", "NotifyDataSetChanged"})
    @Override
    protected void onResume() {
        super.onResume();

        useFocusOutline = pref.getInt("use_focus_outline", 0) == 1;
        if (useFocusOutline) {
            binding.lvMainAll.setBackgroundResource(R.drawable.focus_outline);
            binding.lvMainSettings.setBackgroundResource(R.drawable.focus_outline);
        } else {
            binding.lvMainAll.setBackgroundResource(R.drawable.focus_highlight);
            binding.lvMainSettings.setBackgroundResource(R.drawable.focus_highlight);
        }

        kioskModeActive = pref.getInt("kiosk_mode", 0) == 1;
        adapter.setKioskMode(kioskModeActive);
        if (kioskModeActive) {
            binding.lvMainAll.setVisibility(View.GONE);
            binding.lvMainSettings.setVisibility(View.GONE);
        } else {
            binding.lvMainAll.setVisibility(View.VISIBLE);
            binding.lvMainSettings.setVisibility(View.VISIBLE);
        }

        // check if device rebooted (normal)
        if (pref.getInt("autolaunch_alt_detect", 0) == 0) {
            long lastBootTimestamp = pref.getLong("last_boot_timestamp", 0L);
            long currentBootTimestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            long timestampDifference = Math.abs(currentBootTimestamp - lastBootTimestamp);
            if (timestampDifference > 10000L) {
                // timestamp difference is more than 10 seconds, looks like the device has been rebooted
                //Log.d("Molla", "Device reboot detected");
                pref.edit().putLong("last_boot_timestamp", currentBootTimestamp).apply();
                String autolaunchTargetPackage = pref.getString("autolaunch_package", null);
                if (autolaunchTargetPackage != null) {
                    if (autolaunchTargetPackage.startsWith("custom:")) {
                        var autolaunchTargetCustomItemId = autolaunchTargetPackage.substring(7);
                        var customItems = ((MollaApplication)getApplication()).getCustomShortcutManager().getCustomShortcuts();
                        for (int i = 0; i < customItems.size(); i++) {
                            var customItem = customItems.get(i);
                            if (customItem.customItemIdentifier.equals(autolaunchTargetCustomItemId)) {
                                autoLaunchTarget = customItem;
                                if (!isWaitingForAutoLaunch) initiateAutoLaunch();
                                break;
                            }
                        }
                    } else {
                        ArrayList<String> pnAutoLaunchTarget = new ArrayList<>();
                        pnAutoLaunchTarget.add(autolaunchTargetPackage);
                        AppItem.fetchListOfAppsAsync(this, pnAutoLaunchTarget, (items) -> {
                            if (items.size() == 1) runOnUiThread(() -> {
                                autoLaunchTarget = items.get(0);
                                if (!isWaitingForAutoLaunch) initiateAutoLaunch();
                            });
                        });
                    }
                }
            }
        }

        String orient = pref.getString("forced_orientation", "disable");
        if ("landscape".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        else if ("portrait".equals(orient)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        // enable system bar if goofy setting "use_system_bar" is enabled
        if (pref.getInt("use_system_bar", 0) == 1) {
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());

            ViewCompat.setOnApplyWindowInsetsListener(binding.clMainContainer, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        } else {
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

            ViewCompat.setOnApplyWindowInsetsListener(binding.clMainContainer, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        String favAppsRaw = pref.getString("fav_apps", "");
        ArrayList<String> favApps = new ArrayList<String>(Arrays.asList(favAppsRaw.split("\\?")));

        if (adapter != null) adapter.SetSimpleBackground(pref.getInt("simple_icon_bg", 0) == 1);

        if (isFavListUpdateReserved) {
            binding.pbrMainLoading.setVisibility(View.VISIBLE);
            binding.rvMainFav.setVisibility(View.GONE);
            items.clear();
            adapter.notifyDataSetChanged();
            AppItem.fetchAllAppsAsync(this, (r) -> {
                var csm = ((MollaApplication)getApplication()).getCustomShortcutManager();
                items.clear();
                for (var favApp : favApps) {
                    if (favApp.startsWith("custom:")) {
                        var matchingCustomItem = csm.findCustomShortcutById(favApp.substring(7));
                        if (matchingCustomItem != null) items.add(matchingCustomItem);
                    } else {
                        for (var i : r) if (i.packageName.equals(favApp)) items.add(i);
                    }
                }

                runOnUiThread(() -> {
                    adapter.setKioskMode(kioskModeActive);
                    adapter.notifyDataSetChanged();
                    binding.pbrMainLoading.setVisibility(View.GONE);
                    binding.rvMainFav.setVisibility(View.VISIBLE);
                    binding.tvMainFavName.setVisibility(View.VISIBLE);
                    binding.rvMainFav.scrollToPosition(0);
                    binding.rvMainFav.requestFocus();
                });
            });
            isFavListUpdateReserved = false;
        }

        ((MollaApplication)getApplication()).getWallpaperCache().setWallpaperOnImageView(binding.ivMainWallpaper, false);

        rUpdateStatus.run();

        isCloseable = (pref.getInt("closeable", 0) == 1);
    }

    @SuppressLint("SetTextI18n")
    private void initiateAutoLaunch() {
        final int[] launchDelayResolve = { 0, 3, 5, 10 };
        var launchDelay = pref.getInt("autolaunch_delay", 0);
        if (launchDelay == 0) {
            // launch immediately
            AppItem.launch(this, autoLaunchTarget);
        } else {
            isWaitingForAutoLaunch = true;
            autoLaunchCountdown = launchDelayResolve[launchDelay];
            binding.tvMainAutolaunchOverlayTitle.setText(String.format(getString(R.string.main_autolaunch_overlay_title), autoLaunchTarget.isCustomItem ? autoLaunchTarget.customItemDisplayName : autoLaunchTarget.displayName));
            binding.tvMainAutolaunchOverlaySeconds.setText(autoLaunchCountdown+"");
            setAutoLaunchOverlayVisibility(true);
            h.postDelayed(rCountdownAutoLaunch, 1000);
        }
    }

    private void setAutoLaunchOverlayVisibility(boolean visible) {
        if (visible) {
            binding.rvMainFav.setFocusable(false);
            binding.rvMainFav.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            binding.lvMainAll.setFocusable(false);
            binding.lvMainSettings.setFocusable(false);
            binding.llMainAutolaunchOverlay.setVisibility(View.VISIBLE);
        } else {
            binding.rvMainFav.setFocusable(true);
            binding.rvMainFav.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            binding.lvMainAll.setFocusable(true);
            binding.lvMainSettings.setFocusable(true);
            binding.llMainAutolaunchOverlay.setVisibility(View.GONE);
            binding.lvMainAll.requestFocus();
        }
    }
}
