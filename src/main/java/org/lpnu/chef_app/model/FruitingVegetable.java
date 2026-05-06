package org.lpnu.chef_app.model;

import java.util.Optional;

// Плодові овочі
public class FruitingVegetable extends Vegetable {
    private double waterContentPercent;

    public FruitingVegetable(Long id, String name, double kcal, double p, double f, double c, double wcp) {
        super(id, name, kcal, p, f, c);
        this.waterContentPercent = wcp;
    }

    @Override
    public Optional<String> getPreparationTip() {
        if (waterContentPercent >= 90) {
            return Optional.of("Порада: Має високий вміст води (" + waterContentPercent + "%). Соліть та додавайте безпосередньо перед подачею.");
        }
        return Optional.empty();
    }
}
