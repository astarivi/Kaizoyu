package com.astarivi.kaizolib.common.util;

import java.util.Objects;


public class StringPair {
    private final String name;
    private final String value;

    public StringPair(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StringPair) {
            final StringPair that = (StringPair) obj;
            return this.name.equalsIgnoreCase(that.name) && Objects.equals(this.value, that.value);
        }
        return false;
    }
}