package org.lpnu.chef_app.model.enums;

public enum Allergen {
    NONE("Відсутній"),
    NUTS("Горіхи"),
    LACTOSE("Лактоза"),
    GLUTEN("Глютен"),
    EGGS("Яйця"),
    SESAME("Кунжут");

    private final String displayName;

    Allergen(String displayName) {
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
