// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import android.content.Context;

import java.util.ArrayList;

public interface AppItemLoadCompletedCallback {
    void OnAppItemLoadCompleted(ArrayList<AppItem> result);
}
