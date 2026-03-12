package com.kelab.cloud.marketplace.model;

public enum ProductStatus {

    ACTIVE(true),
    OUT_OF_STOCK(false),
    INACTIVE(false);

    private final boolean sellable;

    ProductStatus(boolean sellable) {
        this.sellable = sellable;
    }

    public boolean isSellable() {
        return sellable;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }

    public boolean isOutOfStock() {
        return this == OUT_OF_STOCK;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}