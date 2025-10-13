// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

public class AppItemCustomIntentExtra {
    private final String name;
    private final Object value;
    private final Class<?> valueType;

    public AppItemCustomIntentExtra(String name, Object value) {
        this.name = name;
        this.value = value;
        this.valueType = value.getClass();
    }

    public String getName() {
        return name;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueAs(Class<T> type) {
        if (type.isInstance(value)) {
            return (T)value;
        } else {
            throw new ClassCastException("Invalid type! Type should be " + valueType);
        }
    }
}
