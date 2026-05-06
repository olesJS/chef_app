package org.lpnu.chef_app.model.enums;

public enum ProcessingState {
    RAW("Сирий"),
    BOILED("Варений"),
    BAKED("Запечений"),
    FRIED("Смажений"),
    STEAMED("На пару"),
    GRILLED("Гриль");

    private final String name;

    ProcessingState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
