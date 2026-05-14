package org.lpnu.chef_app.model.enums;

public enum ProductType {
    ROOT_VEGETABLE("Коренеплід"),
    TUBER_VEGETABLE("Бульбоплід"),
    LEAFY_VEGETABLE("Листовий"),
    FRUITING_VEGETABLE("Плодовий"),
    DRESSING("Заправка"),
    TOPPING("Топінг");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}