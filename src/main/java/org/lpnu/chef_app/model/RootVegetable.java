package org.lpnu.chef_app.model;

import java.util.Optional;

// Коренеплоди
public class RootVegetable extends Vegetable {
    private double sugarContent;

    public RootVegetable(Long id, String name, double kcal, double p, double f, double c, double sugarContent) {
        super(id, name, kcal, p, f, c);
        this.sugarContent = sugarContent;
    }

    @Override
    public Optional<String> getPreparationTip() {
        if (sugarContent > 10) {
            return Optional.of("Має високий вміст цукру (" + sugarContent + "%). Добре смакує з кислими заправками (лимон, оцет).");
        }

        return Optional.empty();
    }
}
