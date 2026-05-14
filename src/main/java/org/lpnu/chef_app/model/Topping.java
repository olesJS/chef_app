package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.Allergen;
import org.lpnu.chef_app.model.enums.ProductType;

import java.util.Optional;

public class Topping extends Product {
    private Allergen allergen;
    private boolean isCrunchy;

    public Topping(Long id, String name, double kcal, double p, double f, double c, Allergen allergen, boolean isCrunchy) {
        super(id, name, kcal, p, f, c);
        this.allergen = allergen;
        this.isCrunchy = isCrunchy;
    }

    public boolean getIsCrunchy() {
        return this.isCrunchy;
    }

    public Allergen getAllergen() {
        return this.allergen;
    }

    @Override
    public Optional<String> getPreparationTip() {
        String allergenInfo = (allergen != null && allergen != Allergen.NONE)
                ? "Містить алерген (" + allergen.getDisplayName() + ")! " : "";
        String textureInfo = isCrunchy
                ? "Додавайте в останню мить, щоб зберегти хрустку текстуру продукту." : "";

        String combined = (allergenInfo + textureInfo).trim();
        return combined.isEmpty() ? Optional.empty() : Optional.of(combined);
    }

    @Override
    public ProductType getType() {
        return ProductType.TOPPING;
    }

}
