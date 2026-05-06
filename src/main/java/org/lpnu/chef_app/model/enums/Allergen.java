package org.lpnu.chef_app.model.enums;

public enum Allergen {
    NONE("Відсутній"),
    NUTS("Горіхи"),
    LACTOSE("Молочні продукти"),
    GLUTEN("Глютен"),
    EGGS("Яйця"),
    SESAME("Кунжут");

    private final String name;

    Allergen(String displayName) {
        this.name = displayName;
    }

    public String getName() {
        return this.name;
    }
}
