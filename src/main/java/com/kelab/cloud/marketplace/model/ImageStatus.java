package com.kelab.cloud.marketplace.model;

public enum ImageStatus {

    ACTIVE(true),
    INACTIVE(false);

    private final boolean visible;

    ImageStatus(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }
}