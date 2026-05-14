package org.lpnu.chef_app.model.enums;

public enum ProcessingState {
    RAW("Сирий", 1.0, 1.0),
    BOILED("Варений", 1.1, 0.9),
    BAKED("Запечений", 0.85, 1.2),
    FRIED("Смажений", 0.8, 1.5),
    STEAMED("На пару", 1.0, 1.0),
    GRILLED("Гриль", 0.9, 1.15);

    private final String displayName;
    private final double massFactor;   // Mass change coefficient
    private final double kcalFactor;   // Kcal change coefficient

    ProcessingState(String displayName, double massFactor, double kcalFactor) {
        this.displayName = displayName;
        this.massFactor = massFactor;
        this.kcalFactor = kcalFactor;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public double getMassFactor() {
        return this.massFactor;
    }

    public double getKcalFactor() {
        return this.kcalFactor;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
