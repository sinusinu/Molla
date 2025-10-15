// Copyright 2022-2025 Woohyun Shin (sinusinu)
// SPDX-License-Identifier: GPL-3.0-only

package com.sinu.molla;

public class AppItemCustomIntentExtra {
    private String name;
    private Object value;
    private Class<?> valueType;

    public AppItemCustomIntentExtra(String name, Object value) {
        this.name = name;
        this.value = value;
        this.valueType = value.getClass();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getValueAsString() {
        if (valueType == String.class) {
            return (String)value;
        } else if (valueType == Integer.class) {
            return Integer.toString((int)value);
        } else if (valueType == Long.class) {
            return Long.toString((long)value);
        } else if (valueType == Float.class) {
            return Float.toString((float)value);
        } else if (valueType == Double.class) {
            return Double.toString((double)value);
        } else if (valueType == Boolean.class) {
            return Boolean.toString((boolean)value);
        } else {
            throw new ClassCastException("Tried to getValueAsString an invalid AppItemCustomIntentExtra");
        }
    }

    public void setValue(String value) {
        valueType = String.class;
        this.value = value;
    }

    public void setValue(int value) {
        valueType = Integer.class;
        this.value = value;
    }

    public void setValue(long value) {
        valueType = Long.class;
        this.value = value;
    }

    public void setValue(float value) {
        valueType = Float.class;
        this.value = value;
    }

    public void setValue(double value) {
        valueType = Double.class;
        this.value = value;
    }

    public void setValue(boolean value) {
        valueType = Boolean.class;
        this.value = value;
    }
}
