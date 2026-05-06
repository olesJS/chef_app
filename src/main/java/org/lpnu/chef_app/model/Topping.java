package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.Allergen;

import java.util.Optional;

public class Topping extends Product {
    private Allergen allergen;
    private boolean isCrunchy;

    public Topping(Long id, String name, double kcal, double p, double f, double c, Allergen allergen, boolean isCrunchy) {
        super(id, name, kcal, p, f, c);
        this.allergen = allergen;
        this.isCrunchy = isCrunchy;
    }

    @Override
    public Optional<String> getPreparationTip() {
        StringBuilder tip = new StringBuilder();

        if (allergen != null && allergen != Allergen.NONE) {
            tip.append("УВАГА: Містить алерген (").append(allergen.getName()).append("). ");
        }

        if (isCrunchy) {
            tip.append("Щоб продукт залишався хрустким, додавайте його в салат безпосередньо перед подачею.");
        }

        return !tip.isEmpty() ? Optional.of(tip.toString().trim()) : Optional.empty();
    }

    public Allergen getAllergen() {
        return this.allergen;
    }
}
