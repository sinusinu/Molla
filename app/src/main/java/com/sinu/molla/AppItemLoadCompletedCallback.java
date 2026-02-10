// Copyright 2022-2026 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

import java.util.ArrayList;

public interface AppItemLoadCompletedCallback {
    void OnAppItemLoadCompleted(ArrayList<AppItem> result);
}
