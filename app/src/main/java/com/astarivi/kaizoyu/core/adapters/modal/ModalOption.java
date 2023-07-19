package com.astarivi.kaizoyu.core.adapters.modal;

import com.astarivi.kaizolib.common.util.StringPair;

import java.util.Objects;


public class ModalOption extends StringPair {
    private final boolean shouldHighlight;

    public ModalOption(final String title, final String description, final boolean shouldHighlight) {
        super(title, description);
        this.shouldHighlight = shouldHighlight;
    }

    public ModalOption(final String title, final String description) {
        super(title, description);
        this.shouldHighlight = false;
    }

    public boolean shouldHighlight() {
        return shouldHighlight;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ModalOption) {
            final ModalOption that = (ModalOption) obj;
            return getName().equalsIgnoreCase(that.getName())
                    && Objects.equals(getValue(), that.getValue())
                    && (shouldHighlight() == that.shouldHighlight());
        }

        return false;
    }
}
